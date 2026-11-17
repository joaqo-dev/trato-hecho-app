package cl.duoc.tratohecho.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tratohecho.data.local.entity.CotizacionEntity
import cl.duoc.tratohecho.data.repository.CotizacionRepository
import cl.duoc.tratohecho.data.sync.GestorSincronizacion
import cl.duoc.tratohecho.domain.CalculadoraIndicadores
import cl.duoc.tratohecho.domain.model.EstadoCotizacion
import cl.duoc.tratohecho.domain.model.Indicadores
import cl.duoc.tratohecho.utils.Formatos
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// Bandejas del inicio: cada una agrupa cotizaciones que piden la misma acción
enum class Bandeja(val titulo: String, val vacio: String) {
    POR_VENCER("Por vencer", "Ninguna cotización vence en los próximos 3 días."),
    ESPERANDO("Esperando respuesta", "No hay cotizaciones esperando respuesta del cliente."),
    BORRADORES("Borradores", "No hay borradores pendientes de enviar."),
    EN_COLA("En cola", "No hay envíos esperando señal.")
}

data class InicioUiState(
    val indicadores: Indicadores,
    val bandejas: Map<Bandeja, List<CotizacionEntity>>,
    val hayCotizaciones: Boolean
) {
    // La bandeja que se abre primero: la más urgente que tenga algo
    val bandejaInicial: Bandeja
        get() = listOf(Bandeja.EN_COLA, Bandeja.POR_VENCER, Bandeja.ESPERANDO, Bandeja.BORRADORES)
            .firstOrNull { !bandejas[it].isNullOrEmpty() } ?: Bandeja.ESPERANDO
}

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
                bandejas = armarBandejas(vigentes),
                hayCotizaciones = vigentes.isNotEmpty()
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val pendientes: StateFlow<Int> = gestorSincronizacion.pendientes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private fun armarBandejas(lista: List<CotizacionEntity>): Map<Bandeja, List<CotizacionEntity>> {
        val enviadas = lista.filter { it.estado == EstadoCotizacion.ENVIADA && !it.envioPendiente }
        val porVencer = enviadas
            .filter { it.fechaVencimiento != null && Formatos.diasHasta(it.fechaVencimiento) in 0..3 }
            .sortedBy { it.fechaVencimiento }
        return mapOf(
            Bandeja.EN_COLA to lista.filter { it.envioPendiente },
            Bandeja.POR_VENCER to porVencer,
            Bandeja.ESPERANDO to (enviadas - porVencer.toSet()).sortedByDescending { it.fechaEnvio ?: 0L },
            Bandeja.BORRADORES to lista
                .filter { it.estado == EstadoCotizacion.BORRADOR && !it.envioPendiente }
                .sortedByDescending { it.fechaCreacion }
        )
    }
}
