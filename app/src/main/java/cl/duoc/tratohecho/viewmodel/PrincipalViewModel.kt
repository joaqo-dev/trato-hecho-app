package cl.duoc.tratohecho.viewmodel

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

    val sincronizando: StateFlow<Boolean> = gestorSincronizacion.sincronizando

    val pendientes: StateFlow<Int> = gestorSincronizacion.pendientes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _mensajes = MutableSharedFlow<String>()
    val mensajes: SharedFlow<String> = _mensajes.asSharedFlow()

    fun sincronizar() {
        viewModelScope.launch {
            gestorSincronizacion.sincronizarAhora()
                .onSuccess { _mensajes.emit("Datos sincronizados") }
                .onFailure { _mensajes.emit(it.message ?: "No se pudo sincronizar") }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch { authRepository.cerrarSesion() }
    }
}
