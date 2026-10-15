package cl.duoc.tratohecho.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tratohecho.data.local.SesionDataStore
import cl.duoc.tratohecho.data.local.entity.UsuarioEntity
import cl.duoc.tratohecho.data.repository.ClienteRepository
import cl.duoc.tratohecho.data.repository.UsuarioRepository
import cl.duoc.tratohecho.data.sync.GestorSincronizacion
import cl.duoc.tratohecho.domain.model.Rol
import cl.duoc.tratohecho.utils.Validaciones
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class UsuarioErrores(
    val nombre: String? = null,
    val correo: String? = null,
    val clave: String? = null,
    val confirmacion: String? = null
) {
    val hayErrores: Boolean get() = listOf(nombre, correo, clave, confirmacion).any { it != null }
}

data class UsuarioFormUiState(
    val id: String = UUID.randomUUID().toString(),
    val esNuevo: Boolean = true,
    val esUsuarioActual: Boolean = false,
    val nombre: String = "",
    val correo: String = "",
    val rol: Rol = Rol.VENDEDOR,
    val rolOriginal: Rol? = null,       // rol con que se cargó (para saber si deja de ser vendedor)
    val activo: Boolean = true,
    val clientesACargo: Int = 0,        // clientes activos que tiene asignados (si es vendedor)
    val clave: String = "",
    val confirmacion: String = "",
    val errores: UsuarioErrores = UsuarioErrores(),
    val intentoGuardar: Boolean = false,
    val guardando: Boolean = false,
    val errorGeneral: String? = null,
    val traspaso: TraspasoUiState? = null
) {
    // Un vendedor con clientes que se da de baja (o cambia de rol) obliga a traspasar sus clientes
    val requiereTraspaso: Boolean
        get() = rolOriginal == Rol.VENDEDOR && clientesACargo > 0 && (!activo || rol != Rol.VENDEDOR)
}

// Diálogo para elegir a qué vendedor pasan los clientes de quien se da de baja
data class TraspasoUiState(
    val seleccionadoId: String? = null,
    val error: String? = null
)

@HiltViewModel
class UsuarioFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val usuarioRepository: UsuarioRepository,
    private val clienteRepository: ClienteRepository,
    private val gestorSincronizacion: GestorSincronizacion,
    private val sesionDataStore: SesionDataStore
) : ViewModel() {


    private val _estado = MutableStateFlow(UsuarioFormUiState())
    val estado: StateFlow<UsuarioFormUiState> = _estado.asStateFlow()

    // Vendedores activos que pueden recibir los clientes (sin contar al que se da de baja)
    val vendedoresDisponibles: StateFlow<List<UsuarioEntity>> =
        combine(usuarioRepository.observarUsuarios(), _estado) { lista, e ->
            lista.filter { it.rol == Rol.VENDEDOR && it.activo && it.id != e.id }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _guardado = MutableSharedFlow<String>()
    val guardado: SharedFlow<String> = _guardado.asSharedFlow()

    init {
        val id: String? = savedStateHandle["id"]
        if (id != null) {
            viewModelScope.launch {
                val sesion = sesionDataStore.obtenerSesion()
                usuarioRepository.obtenerUsuario(id)?.let { u ->
                    _estado.value = UsuarioFormUiState(
                        id = u.id, esNuevo = false, esUsuarioActual = u.id == sesion?.usuarioId,
                        nombre = u.nombre, correo = u.correo, rol = u.rol, rolOriginal = u.rol, activo = u.activo,
                        clientesACargo = if (u.rol == Rol.VENDEDOR) clienteRepository.contarActivosDe(u.id) else 0
                    )
                }
            }
        }
    }

    private fun actualizar(cambio: (UsuarioFormUiState) -> UsuarioFormUiState) = _estado.update { actual ->
        val nuevo = cambio(actual).copy(errorGeneral = null)
        if (nuevo.intentoGuardar) nuevo.copy(errores = validar(nuevo)) else nuevo
    }

    fun onNombreChange(v: String) = actualizar { it.copy(nombre = v) }
    fun onCorreoChange(v: String) = actualizar { it.copy(correo = v) }
    fun onRolChange(v: Rol) = actualizar { it.copy(rol = v) }
    fun onActivoChange(v: Boolean) = actualizar { it.copy(activo = v) }
    fun onClaveChange(v: String) = actualizar { it.copy(clave = v) }
    fun onConfirmacionChange(v: String) = actualizar { it.copy(confirmacion = v) }

    // Al editar, la clave es opcional (vacía = se mantiene la actual)
    private fun validar(e: UsuarioFormUiState): UsuarioErrores {
        val validarClave = e.esNuevo || e.clave.isNotEmpty()
        return UsuarioErrores(
            nombre = Validaciones.largo(e.nombre, "El nombre", 3, 60),
            correo = Validaciones.correo(e.correo),
            clave = if (validarClave) Validaciones.claveNueva(e.clave) else null,
            confirmacion = if (validarClave && e.confirmacion != e.clave) "Las claves no coinciden" else null
        )
    }

    fun guardar() {
        val actual = _estado.value
        val errores = validar(actual)
        _estado.update { it.copy(errores = errores, intentoGuardar = true) }
        if (errores.hayErrores) return
        // Para no dejar clientes sin vendedor, primero se eligen a quién pasan
        if (actual.requiereTraspaso) {
            _estado.update { it.copy(traspaso = TraspasoUiState()) }
            return
        }
        guardarUsuario()
    }

    fun onVendedorTraspaso(id: String) = _estado.update { it.copy(traspaso = it.traspaso?.copy(seleccionadoId = id, error = null)) }

    fun cerrarTraspaso() = _estado.update { it.copy(traspaso = null) }

    // Traspasa los clientes al vendedor elegido y recién ahí guarda la baja
    fun confirmarTraspaso() {
        val actual = _estado.value
        val destino = actual.traspaso?.seleccionadoId
        if (destino == null) {
            _estado.update { it.copy(traspaso = it.traspaso?.copy(error = "Elige el vendedor que recibirá los clientes")) }
            return
        }
        viewModelScope.launch {
            _estado.update { it.copy(guardando = true) }
            usuarioRepository.traspasarClientes(actual.id, destino)
                .onSuccess {
                    _estado.update { it.copy(traspaso = null, clientesACargo = 0) }
                    guardarUsuario()
                }
                .onFailure { error ->
                    _estado.update { it.copy(guardando = false, traspaso = it.traspaso?.copy(error = error.message)) }
                }
        }
    }

    private fun guardarUsuario() {
        val actual = _estado.value
        viewModelScope.launch {
            _estado.update { it.copy(guardando = true) }
            val usuario = UsuarioEntity(
                id = actual.id,
                empresaId = sesionDataStore.obtenerSesion()?.empresaId ?: "",
                nombre = actual.nombre.trim(),
                correo = actual.correo.trim().lowercase(),
                rol = actual.rol,
                activo = actual.activo
            )
            usuarioRepository.guardar(usuario, actual.clave.ifBlank { null })
                .onSuccess {
                    // Se bajan los clientes con su nuevo vendedor a cargo
                    gestorSincronizacion.sincronizarEnSegundoPlano()
                    _guardado.emit(
                        when {
                            actual.esNuevo -> "Usuario creado"
                            !actual.activo -> "${actual.nombre} quedó dado de baja. Su historial se conserva."
                            else -> "Usuario actualizado"
                        }
                    )
                }
                .onFailure { error -> _estado.update { it.copy(errorGeneral = error.message) } }
            _estado.update { it.copy(guardando = false) }
        }
    }
}
