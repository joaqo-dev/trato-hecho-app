package cl.duoc.tratohecho.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tratohecho.data.local.entity.ClienteConContactos
import cl.duoc.tratohecho.data.local.entity.CotizacionEntity
import cl.duoc.tratohecho.data.local.entity.UsuarioEntity
import cl.duoc.tratohecho.data.repository.ClienteRepository
import cl.duoc.tratohecho.data.repository.CotizacionRepository
import cl.duoc.tratohecho.data.sync.GestorSincronizacion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Estado del diálogo para reasignar el cliente a otro vendedor
data class ReasignarUiState(
    val cargando: Boolean = true,
    val vendedores: List<UsuarioEntity> = emptyList(),
    val seleccionadoId: String? = null,
    val guardando: Boolean = false,
    val error: String? = null
)

sealed class EventoDetalleCliente {
    data class Mensaje(val texto: String) : EventoDetalleCliente()
}

@HiltViewModel
class ClienteDetalleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val clienteRepository: ClienteRepository,
    private val gestorSincronizacion: GestorSincronizacion,
    cotizacionRepository: CotizacionRepository
) : ViewModel() {

    private val clienteId: String = checkNotNull(savedStateHandle["id"])

    val cliente: StateFlow<ClienteConContactos?> = clienteRepository.observarCliente(clienteId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val cotizaciones: StateFlow<List<CotizacionEntity>> = cotizacionRepository.observarCotizaciones()
        .map { lista -> lista.filter { it.clienteId == clienteId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // null = diálogo cerrado
    private val _reasignar = MutableStateFlow<ReasignarUiState?>(null)
    val reasignar: StateFlow<ReasignarUiState?> = _reasignar.asStateFlow()

    private val _eventos = MutableSharedFlow<EventoDetalleCliente>()
    val eventos: SharedFlow<EventoDetalleCliente> = _eventos.asSharedFlow()

    fun abrirReasignar() {
        val actualId = cliente.value?.cliente?.vendedorId
        _reasignar.value = ReasignarUiState(seleccionadoId = actualId)
        viewModelScope.launch {
            clienteRepository.obtenerVendedores()
                .onSuccess { lista -> _reasignar.update { it?.copy(cargando = false, vendedores = lista) } }
                .onFailure { error -> _reasignar.update { it?.copy(cargando = false, error = error.message) } }
        }
    }

    fun cerrarReasignar() {
        _reasignar.value = null
    }

    fun onVendedorSeleccionado(id: String) = _reasignar.update { it?.copy(seleccionadoId = id, error = null) }

    fun confirmarReasignar() {
        val dialogo = _reasignar.value ?: return
        val nuevoId = dialogo.seleccionadoId
        // Validación: debe elegirse un vendedor distinto al actual
        if (nuevoId == null || nuevoId == cliente.value?.cliente?.vendedorId) {
            _reasignar.update { it?.copy(error = "Elige un vendedor distinto al actual") }
            return
        }
        viewModelScope.launch {
            _reasignar.update { it?.copy(guardando = true, error = null) }
            clienteRepository.reasignar(clienteId, nuevoId)
                .onSuccess { reasignado ->
                    _reasignar.value = null
                    _eventos.emit(EventoDetalleCliente.Mensaje("Cliente reasignado a ${reasignado.cliente.vendedorNombre}"))
                }
                .onFailure { error -> _reasignar.update { it?.copy(guardando = false, error = error.message) } }
        }
    }

    // Baja lógica o reactivación (solo administrador); funciona sin conexión y se sincroniza después
    fun cambiarEstado(activo: Boolean) {
        viewModelScope.launch {
            clienteRepository.cambiarEstado(clienteId, activo)
            gestorSincronizacion.sincronizarEnSegundoPlano()
            _eventos.emit(
                EventoDetalleCliente.Mensaje(
                    if (activo) "Cliente reactivado" else "Cliente dado de baja. Su historial se conserva."
                )
            )
        }
    }
}
