package cl.duoc.tratohecho.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tratohecho.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed class EstadoInicio {
    data object Cargando : EstadoInicio()
    data object SinSesion : EstadoInicio()
    data object ConSesion : EstadoInicio()
}

// Decide si la app parte en el login o directo en la pantalla principal
@HiltViewModel
class MainViewModel @Inject constructor(
    authRepository: AuthRepository
) : ViewModel() {
    val estadoInicio: StateFlow<EstadoInicio> = authRepository.sesion
        .map { if (it == null) EstadoInicio.SinSesion else EstadoInicio.ConSesion }
        .stateIn(viewModelScope, SharingStarted.Eagerly, EstadoInicio.Cargando)
}
