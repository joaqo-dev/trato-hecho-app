package cl.duoc.tratohecho.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tratohecho.data.local.entity.CotizacionEntity
import cl.duoc.tratohecho.data.repository.CotizacionRepository
import cl.duoc.tratohecho.data.sync.GestorSincronizacion
import cl.duoc.tratohecho.domain.CalculadoraIndicadores
import cl.duoc.tratohecho.domain.model.EstadoCotizacion
import cl.duoc.tratohecho.domain.model.Indicadores
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class InicioUiState(
    val indicadores: Indicadores,
    val enCurso: CotizacionEntity?,          // la cotización que el vendedor está siguiendo ahora
    val recientes: List<CotizacionEntity>    // últimas cotizaciones creadas
)

@HiltViewModel
class InicioViewModel @Inject constructor(
    cotizacionRepository: CotizacionRepository,
    gestorSincronizacion: GestorSincronizacion
) : ViewModel() {

    // Se recalcula solo cada vez que cambian las cotizaciones en Room
    val estado: StateFlow<InicioUiState?> = cotizacionRepository.observarCotizaciones()
        .map { lista ->
            val vigentes = lista.filter { it.estado != EstadoCotizacion.REEMPLAZADA }
            InicioUiState(
                indicadores = CalculadoraIndicadores.calcular(lista),
                enCurso = buscarEnCurso(vigentes),
                recientes = vigentes.take(3)
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val pendientes: StateFlow<Int> = gestorSincronizacion.pendientes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val sincronizando: StateFlow<Boolean> = gestorSincronizacion.sincronizando

    // Primero la que está en cola de envío, luego la última enviada esperando respuesta, el último borrador
    // y, si no hay ninguna, la última aceptada que espera facturación
    private fun buscarEnCurso(lista: List<CotizacionEntity>): CotizacionEntity? =
        lista.firstOrNull { it.envioPendiente }
            ?: lista.filter { it.estado == EstadoCotizacion.ENVIADA }.maxByOrNull { it.fechaEnvio ?: 0L }
            ?: lista.firstOrNull { it.estado == EstadoCotizacion.BORRADOR }
            ?: lista.filter { it.estado == EstadoCotizacion.ACEPTADA }.maxByOrNull { it.fechaRespuesta ?: 0L }
}
