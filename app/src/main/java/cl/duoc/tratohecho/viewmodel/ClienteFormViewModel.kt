package cl.duoc.tratohecho.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tratohecho.data.local.SesionDataStore
import cl.duoc.tratohecho.data.local.entity.ClienteEntity
import cl.duoc.tratohecho.data.local.entity.ContactoEntity
import cl.duoc.tratohecho.data.repository.ClienteRepository
import cl.duoc.tratohecho.data.sync.GestorSincronizacion
import cl.duoc.tratohecho.nativo.MonitorRed
import cl.duoc.tratohecho.utils.Validaciones
import cl.duoc.tratohecho.utils.ValidadorRut
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

data class ClienteErrores(
    val rut: String? = null,
    val razonSocial: String? = null,
    val giro: String? = null,
    val direccion: String? = null,
    val correo: String? = null,
    val telefono: String? = null
) {
    val hayErrores: Boolean
        get() = listOf(rut, razonSocial, giro, direccion, correo, telefono).any { it != null }
}

data class ContactoErrores(
    val nombre: String? = null,
    val cargo: String? = null,
    val correo: String? = null,
    val telefono: String? = null
) {
    val hayErrores: Boolean get() = listOf(nombre, cargo, correo, telefono).any { it != null }
}

// Estado del diálogo para agregar/editar un contacto del cliente
data class ContactoFormState(
    val id: String = UUID.randomUUID().toString(),
    val esNuevo: Boolean = true,
    val nombre: String = "",
    val cargo: String = "",
    val correo: String = "",
    val telefono: String = "+56 9 ",
    val errores: ContactoErrores = ContactoErrores(),
    val intentoGuardar: Boolean = false
)

data class ClienteFormUiState(
    val id: String = UUID.randomUUID().toString(),
    val esNuevo: Boolean = true,
    val cargando: Boolean = false,
    val empresaId: String = "",
    val vendedorId: String = "",
    val vendedorNombre: String = "",
    val rut: String = "",
    val razonSocial: String = "",
    val giro: String = "",
    val direccion: String = "",
    val correo: String = "",
    val telefono: String = "+56 9 ",
    val contactos: List<ContactoEntity> = emptyList(),
    val errores: ClienteErrores = ClienteErrores(),
    val intentoGuardar: Boolean = false,
    val guardando: Boolean = false,
    val dialogoContacto: ContactoFormState? = null
)

@HiltViewModel
class ClienteFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val clienteRepository: ClienteRepository,
    private val sesionDataStore: SesionDataStore,
    private val gestorSincronizacion: GestorSincronizacion,
    private val monitorRed: MonitorRed
) : ViewModel() {

    private val _estado = MutableStateFlow(ClienteFormUiState(cargando = true))
    val estado: StateFlow<ClienteFormUiState> = _estado.asStateFlow()

    private val _guardado = MutableSharedFlow<String>()
    val guardado: SharedFlow<String> = _guardado.asSharedFlow()

    init {
        val id: String? = savedStateHandle["id"]
        viewModelScope.launch {
            val sesion = sesionDataStore.obtenerSesion()
            val existente = id?.let { clienteRepository.obtenerCliente(it) }
            _estado.value = if (existente != null) {
                val c = existente.cliente
                ClienteFormUiState(
                    id = c.id, esNuevo = false, empresaId = c.empresaId, vendedorId = c.vendedorId, vendedorNombre = c.vendedorNombre,
                    rut = c.rut, razonSocial = c.razonSocial, giro = c.giro, direccion = c.direccion,
                    correo = c.correo, telefono = c.telefono, contactos = existente.contactos
                )
            } else {
                ClienteFormUiState(
                    empresaId = sesion?.empresaId ?: "",
                    vendedorId = sesion?.usuarioId ?: "",
                    vendedorNombre = sesion?.nombre ?: ""
                )
            }
        }
    }

    // ---------- Campos del cliente (se revalidan en tiempo real después del primer intento) ----------

    private fun actualizar(cambio: (ClienteFormUiState) -> ClienteFormUiState) {
        _estado.update { actual ->
            val nuevo = cambio(actual)
            if (nuevo.intentoGuardar) nuevo.copy(errores = validarCliente(nuevo)) else nuevo
        }
    }

    fun onRutChange(v: String) = actualizar { it.copy(rut = v) }
    fun onRazonSocialChange(v: String) = actualizar { it.copy(razonSocial = v) }
    fun onGiroChange(v: String) = actualizar { it.copy(giro = v) }
    fun onDireccionChange(v: String) = actualizar { it.copy(direccion = v) }
    fun onCorreoChange(v: String) = actualizar { it.copy(correo = v) }
    fun onTelefonoChange(v: String) = actualizar { it.copy(telefono = v) }

    // Al salir del campo RUT se le da formato 76.123.456-0
    fun formatearRut() {
        val rut = _estado.value.rut
        if (ValidadorRut.esValido(rut)) actualizar { it.copy(rut = ValidadorRut.formatear(rut)) }
    }

    private fun validarCliente(e: ClienteFormUiState) = ClienteErrores(
        rut = Validaciones.rut(e.rut),
        razonSocial = Validaciones.largo(e.razonSocial, "La razón social", 3, 80),
        giro = Validaciones.largo(e.giro, "El giro", 3, 60),
        direccion = Validaciones.largo(e.direccion, "La dirección", 5, 100),
        correo = Validaciones.correo(e.correo),
        telefono = Validaciones.telefono(e.telefono)
    )

    fun guardar() {
        val actual = _estado.value
        var errores = validarCliente(actual)
        viewModelScope.launch {
            if (errores.rut == null && clienteRepository.existeRut(ValidadorRut.formatear(actual.rut), actual.id)) {
                errores = errores.copy(rut = "Ya existe un cliente con este RUT")
            }
            _estado.update { it.copy(errores = errores, intentoGuardar = true) }
            if (errores.hayErrores) return@launch

            _estado.update { it.copy(guardando = true) }
            val cliente = ClienteEntity(
                id = actual.id,
                empresaId = actual.empresaId,
                vendedorId = actual.vendedorId,
                rut = ValidadorRut.formatear(actual.rut),
                razonSocial = actual.razonSocial.trim(),
                giro = actual.giro.trim(),
                direccion = actual.direccion.trim(),
                correo = actual.correo.trim(),
                telefono = actual.telefono.trim(),
                vendedorNombre = actual.vendedorNombre
            )
            clienteRepository.guardar(cliente, actual.contactos)
            gestorSincronizacion.sincronizarEnSegundoPlano()
            _estado.update { it.copy(guardando = false) }
            _guardado.emit(
                if (monitorRed.estaConectado()) "Cliente guardado"
                else "Cliente guardado en el teléfono. Se sincronizará al volver la señal."
            )
        }
    }

    // ---------- Contactos ----------

    fun abrirNuevoContacto() = _estado.update { it.copy(dialogoContacto = ContactoFormState()) }

    fun editarContacto(contacto: ContactoEntity) = _estado.update {
        it.copy(
            dialogoContacto = ContactoFormState(
                id = contacto.id, esNuevo = false, nombre = contacto.nombre, cargo = contacto.cargo,
                correo = contacto.correo, telefono = contacto.telefono
            )
        )
    }

    fun cerrarContacto() = _estado.update { it.copy(dialogoContacto = null) }

    private fun actualizarContacto(cambio: (ContactoFormState) -> ContactoFormState) {
        _estado.update { actual ->
            val dialogo = actual.dialogoContacto ?: return@update actual
            val nuevo = cambio(dialogo)
            actual.copy(dialogoContacto = if (nuevo.intentoGuardar) nuevo.copy(errores = validarContacto(nuevo)) else nuevo)
        }
    }

    fun onContactoNombreChange(v: String) = actualizarContacto { it.copy(nombre = v) }
    fun onContactoCargoChange(v: String) = actualizarContacto { it.copy(cargo = v) }
    fun onContactoCorreoChange(v: String) = actualizarContacto { it.copy(correo = v) }
    fun onContactoTelefonoChange(v: String) = actualizarContacto { it.copy(telefono = v) }

    private fun validarContacto(c: ContactoFormState) = ContactoErrores(
        nombre = Validaciones.largo(c.nombre, "El nombre", 3, 60),
        cargo = Validaciones.largo(c.cargo, "El cargo", 2, 40),
        correo = Validaciones.correo(c.correo),
        telefono = Validaciones.telefono(c.telefono)
    )

    fun guardarContacto() {
        val dialogo = _estado.value.dialogoContacto ?: return
        val errores = validarContacto(dialogo)
        if (errores.hayErrores) {
            _estado.update { it.copy(dialogoContacto = dialogo.copy(errores = errores, intentoGuardar = true)) }
            return
        }
        val contacto = ContactoEntity(
            id = dialogo.id,
            clienteId = _estado.value.id,
            nombre = dialogo.nombre.trim(),
            cargo = dialogo.cargo.trim(),
            correo = dialogo.correo.trim(),
            telefono = dialogo.telefono.trim()
        )
        _estado.update { actual ->
            val lista = actual.contactos.filter { it.id != contacto.id } + contacto
            actual.copy(contactos = lista, dialogoContacto = null)
        }
    }

    fun eliminarContacto(contacto: ContactoEntity) = _estado.update { actual ->
        actual.copy(contactos = actual.contactos.filter { it.id != contacto.id })
    }
}
