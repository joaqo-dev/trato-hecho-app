package cl.duoc.tratohecho.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tratohecho.data.local.entity.CotizacionEntity
import cl.duoc.tratohecho.data.repository.CotizacionRepository
import cl.duoc.tratohecho.domain.model.EstadoCotizacion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CotizacionesViewModel @Inject constructor(
    cotizacionRepository: CotizacionRepository
) : ViewModel() {

    // null = todas las vigentes (sin las versiones reemplazadas)
    private val _filtro = MutableStateFlow<EstadoCotizacion?>(null)
    val filtro: StateFlow<EstadoCotizacion?> = _filtro.asStateFlow()

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    val cotizaciones: StateFlow<List<CotizacionEntity>?> =
        combine(cotizacionRepository.observarCotizaciones(), _filtro, _busqueda) { lista, filtro, texto ->
            val porEstado = if (filtro == null) lista.filter { it.estado != EstadoCotizacion.REEMPLAZADA }
            else lista.filter { it.estado == filtro }
            if (texto.isBlank()) porEstado
            else porEstado.filter {
                it.numeroCompleto.contains(texto, ignoreCase = true) || it.clienteNombre.contains(texto, ignoreCase = true)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun onBusquedaChange(texto: String) {
        _busqueda.value = texto
    }

    fun onFiltroChange(estado: EstadoCotizacion?) {
        _filtro.value = if (_filtro.value == estado) null else estado
    }
}
