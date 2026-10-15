package cl.duoc.tratohecho.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tratohecho.data.repository.CotizacionRepository
import cl.duoc.tratohecho.data.sync.GestorSincronizacion
import cl.duoc.tratohecho.domain.CalculadoraIndicadores
import cl.duoc.tratohecho.domain.model.Indicadores
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class InicioViewModel @Inject constructor(
    cotizacionRepository: CotizacionRepository,
    gestorSincronizacion: GestorSincronizacion
) : ViewModel() {

    // Se recalcula solo cada vez que cambian las cotizaciones en Room
    val indicadores: StateFlow<Indicadores?> = cotizacionRepository.observarCotizaciones()
        .map { CalculadoraIndicadores.calcular(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val pendientes: StateFlow<Int> = gestorSincronizacion.pendientes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val sincronizando: StateFlow<Boolean> = gestorSincronizacion.sincronizando
}
