package cl.duoc.tratohecho.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tratohecho.data.local.entity.ClienteConContactos
import cl.duoc.tratohecho.data.repository.ClienteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ClientesViewModel @Inject constructor(
    clienteRepository: ClienteRepository
) : ViewModel() {

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    val clientes: StateFlow<List<ClienteConContactos>?> =
        combine(clienteRepository.observarClientes(), _busqueda) { lista, texto ->
            if (texto.isBlank()) lista
            else lista.filter {
                it.cliente.razonSocial.contains(texto, ignoreCase = true) || it.cliente.rut.contains(texto)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun onBusquedaChange(texto: String) {
        _busqueda.value = texto
    }
}
