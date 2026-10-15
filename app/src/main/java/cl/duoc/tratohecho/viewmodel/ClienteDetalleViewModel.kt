package cl.duoc.tratohecho.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tratohecho.data.local.entity.ClienteConContactos
import cl.duoc.tratohecho.data.local.entity.CotizacionEntity
import cl.duoc.tratohecho.data.repository.ClienteRepository
import cl.duoc.tratohecho.data.repository.CotizacionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class EventoDetalleCliente {
    data class Mensaje(val texto: String) : EventoDetalleCliente()
    data object Eliminado : EventoDetalleCliente()
}

@HiltViewModel
class ClienteDetalleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val clienteRepository: ClienteRepository,
    cotizacionRepository: CotizacionRepository
) : ViewModel() {

    private val clienteId: String = checkNotNull(savedStateHandle["id"])

    val cliente: StateFlow<ClienteConContactos?> = clienteRepository.observarCliente(clienteId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val cotizaciones: StateFlow<List<CotizacionEntity>> = cotizacionRepository.observarCotizaciones()
        .map { lista -> lista.filter { it.clienteId == clienteId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _eventos = MutableSharedFlow<EventoDetalleCliente>()
    val eventos: SharedFlow<EventoDetalleCliente> = _eventos.asSharedFlow()

    fun eliminar() {
        viewModelScope.launch {
            clienteRepository.eliminar(clienteId)
                .onSuccess { _eventos.emit(EventoDetalleCliente.Eliminado) }
                .onFailure { _eventos.emit(EventoDetalleCliente.Mensaje(it.message ?: "No se pudo eliminar")) }
        }
    }
}
