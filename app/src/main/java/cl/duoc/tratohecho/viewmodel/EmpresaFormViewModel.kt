package cl.duoc.tratohecho.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tratohecho.data.local.entity.EmpresaEntity
import cl.duoc.tratohecho.data.repository.EmpresaRepository
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
import javax.inject.Inject

data class EmpresaErrores(
    val razonSocial: String? = null,
    val rut: String? = null,
    val giro: String? = null,
    val direccion: String? = null,
    val correo: String? = null,
    val telefono: String? = null
) {
    val hayErrores: Boolean get() = listOf(razonSocial, rut, giro, direccion, correo, telefono).any { it != null }
}

data class EmpresaFormUiState(
    val id: String = "",
    val cargando: Boolean = true,
    val razonSocial: String = "",
    val rut: String = "",
    val giro: String = "",
    val direccion: String = "",
    val correo: String = "",
    val telefono: String = "",
    val errores: EmpresaErrores = EmpresaErrores(),
    val intentoGuardar: Boolean = false,
    val guardando: Boolean = false,
    val errorGeneral: String? = null
)

// Datos con que la empresa se identifica como emisor en sus cotizaciones (aparecen en el PDF)
@HiltViewModel
class EmpresaFormViewModel @Inject constructor(
    private val empresaRepository: EmpresaRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(EmpresaFormUiState())
    val estado: StateFlow<EmpresaFormUiState> = _estado.asStateFlow()

    private val _guardado = MutableSharedFlow<String>()
    val guardado: SharedFlow<String> = _guardado.asSharedFlow()

    init {
        viewModelScope.launch {
            val e = empresaRepository.obtenerEmpresa()
            _estado.value = if (e == null) EmpresaFormUiState(cargando = false) else EmpresaFormUiState(
                id = e.id, cargando = false, razonSocial = e.razonSocial, rut = e.rut, giro = e.giro,
                direccion = e.direccion, correo = e.correo, telefono = e.telefono
            )
        }
    }

    private fun actualizar(cambio: (EmpresaFormUiState) -> EmpresaFormUiState) = _estado.update { actual ->
        val nuevo = cambio(actual).copy(errorGeneral = null)
        if (nuevo.intentoGuardar) nuevo.copy(errores = validar(nuevo)) else nuevo
    }

    fun onRazonSocialChange(v: String) = actualizar { it.copy(razonSocial = v) }
    fun onRutChange(v: String) = actualizar { it.copy(rut = v) }
    fun onGiroChange(v: String) = actualizar { it.copy(giro = v) }
    fun onDireccionChange(v: String) = actualizar { it.copy(direccion = v) }
    fun onCorreoChange(v: String) = actualizar { it.copy(correo = v) }
    fun onTelefonoChange(v: String) = actualizar { it.copy(telefono = v) }

    fun formatearRut() {
        val rut = _estado.value.rut
        if (ValidadorRut.esValido(rut)) actualizar { it.copy(rut = ValidadorRut.formatear(rut)) }
    }

    private fun validar(e: EmpresaFormUiState) = EmpresaErrores(
        razonSocial = Validaciones.largo(e.razonSocial, "La razón social", 3, 80),
        rut = Validaciones.rut(e.rut),
        giro = Validaciones.largo(e.giro, "El giro", 3, 60),
        direccion = Validaciones.largo(e.direccion, "La dirección", 5, 100),
        correo = Validaciones.correo(e.correo),
        telefono = Validaciones.telefono(e.telefono)
    )

    fun guardar() {
        val actual = _estado.value
        val errores = validar(actual)
        _estado.update { it.copy(errores = errores, intentoGuardar = true) }
        if (errores.hayErrores) return
        viewModelScope.launch {
            _estado.update { it.copy(guardando = true) }
            val empresa = EmpresaEntity(
                id = actual.id,
                razonSocial = actual.razonSocial.trim(),
                rut = ValidadorRut.formatear(actual.rut),
                giro = actual.giro.trim(),
                direccion = actual.direccion.trim(),
                correo = actual.correo.trim(),
                telefono = actual.telefono.trim()
            )
            empresaRepository.guardar(empresa)
                .onSuccess { _guardado.emit("Datos de la empresa actualizados") }
                .onFailure { error -> _estado.update { it.copy(errorGeneral = error.message) } }
            _estado.update { it.copy(guardando = false) }
        }
    }
}
