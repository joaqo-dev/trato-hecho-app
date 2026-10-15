package cl.duoc.tratohecho.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tratohecho.data.repository.AuthRepository
import cl.duoc.tratohecho.data.sync.GestorSincronizacion
import cl.duoc.tratohecho.utils.Validaciones
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val correo: String = "",
    val clave: String = "",
    val errorCorreo: String? = null,
    val errorClave: String? = null,
    val intentoEnviar: Boolean = false,
    val cargando: Boolean = false,
    val errorGeneral: String? = null,
    val ingresoExitoso: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val gestorSincronizacion: GestorSincronizacion
) : ViewModel() {

    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    // Después del primer intento, los errores se actualizan en tiempo real mientras se escribe
    fun onCorreoChange(valor: String) = _estado.update {
        it.copy(correo = valor, errorCorreo = if (it.intentoEnviar) Validaciones.correo(valor) else null, errorGeneral = null)
    }

    fun onClaveChange(valor: String) = _estado.update {
        it.copy(clave = valor, errorClave = if (it.intentoEnviar) Validaciones.clave(valor) else null, errorGeneral = null)
    }

    private fun validar(): Boolean {
        val actual = _estado.value
        val errorCorreo = Validaciones.correo(actual.correo)
        val errorClave = Validaciones.clave(actual.clave)
        _estado.update { it.copy(errorCorreo = errorCorreo, errorClave = errorClave, intentoEnviar = true) }
        return errorCorreo == null && errorClave == null
    }

    fun iniciarSesion() {
        if (!validar()) return
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, errorGeneral = null) }
            authRepository.login(_estado.value.correo, _estado.value.clave)
                .onSuccess {
                    // Primera descarga de datos de la empresa
                    gestorSincronizacion.sincronizarAhora()
                    _estado.update { it.copy(cargando = false, ingresoExitoso = true) }
                }
                .onFailure { error ->
                    _estado.update { it.copy(cargando = false, errorGeneral = error.message) }
                }
        }
    }
}
