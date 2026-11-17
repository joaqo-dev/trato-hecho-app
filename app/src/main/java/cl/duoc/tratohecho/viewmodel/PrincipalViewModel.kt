package cl.duoc.tratohecho.viewmodel

import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tratohecho.data.repository.AuthRepository
import cl.duoc.tratohecho.data.sync.GestorSincronizacion
import cl.duoc.tratohecho.domain.model.Sesion
import cl.duoc.tratohecho.nativo.MonitorRed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// Estado compartido por todas las secciones: sesión, conexión y sincronización
@HiltViewModel
class PrincipalViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val gestorSincronizacion: GestorSincronizacion,
    monitorRed: MonitorRed
) : ViewModel() {

    val sesion: StateFlow<Sesion?> = authRepository.sesion
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val hayConexion: StateFlow<Boolean> = monitorRed.hayConexion

    // Sincronización automática (al abrir la app o al volver la señal): no se muestra en pantalla,
    // solo sirve para decir "Cargando…" cuando una lista aún está vacía
    val sincronizando: StateFlow<Boolean> = gestorSincronizacion.sincronizando

    // true solo mientras dura una actualización que pidió el usuario deslizando hacia abajo:
    // es lo único que muestra la rueda de carga
    private val _refrescando = MutableStateFlow(false)
    val refrescando: StateFlow<Boolean> = _refrescando.asStateFlow()

    val pendientes: StateFlow<Int> = gestorSincronizacion.pendientes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _mensajes = MutableSharedFlow<String>()
    val mensajes: SharedFlow<String> = _mensajes.asSharedFlow()

    fun sincronizar() {
        if (_refrescando.value) return
        viewModelScope.launch {
            _refrescando.value = true
            // Al terminar, la rueda se va sola: solo se avisa con un mensaje si algo falló
            gestorSincronizacion.sincronizarAhora()
                .onFailure { _mensajes.emit(it.message ?: "No se pudo actualizar") }
            _refrescando.value = false
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch { authRepository.cerrarSesion() }
    }
}
