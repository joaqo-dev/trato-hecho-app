package cl.duoc.tratohecho.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tratohecho.data.local.SesionDataStore
import cl.duoc.tratohecho.data.local.entity.UsuarioEntity
import cl.duoc.tratohecho.data.repository.UsuarioRepository
import cl.duoc.tratohecho.domain.model.Rol
import cl.duoc.tratohecho.utils.Validaciones
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
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
    val activo: Boolean = true,
    val clave: String = "",
    val confirmacion: String = "",
    val errores: UsuarioErrores = UsuarioErrores(),
    val intentoGuardar: Boolean = false,
    val guardando: Boolean = false,
    val errorGeneral: String? = null
)

@HiltViewModel
class UsuarioFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val usuarioRepository: UsuarioRepository,
    private val sesionDataStore: SesionDataStore
) : ViewModel() {

    private val _estado = MutableStateFlow(UsuarioFormUiState())
    val estado: StateFlow<UsuarioFormUiState> = _estado.asStateFlow()

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
                        nombre = u.nombre, correo = u.correo, rol = u.rol, activo = u.activo
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
                .onSuccess { _guardado.emit(if (actual.esNuevo) "Usuario creado" else "Usuario actualizado") }
                .onFailure { error -> _estado.update { it.copy(errorGeneral = error.message) } }
            _estado.update { it.copy(guardando = false) }
        }
    }
}
