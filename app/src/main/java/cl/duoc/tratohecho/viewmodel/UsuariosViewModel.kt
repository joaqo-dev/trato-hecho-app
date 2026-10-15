package cl.duoc.tratohecho.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tratohecho.data.local.entity.UsuarioEntity
import cl.duoc.tratohecho.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsuariosViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    val usuarios: StateFlow<List<UsuarioEntity>> = usuarioRepository.observarUsuarios()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val _mensajes = MutableSharedFlow<String>()
    val mensajes: SharedFlow<String> = _mensajes.asSharedFlow()

    init {
        refrescar()
    }

    fun refrescar() {
        viewModelScope.launch {
            _cargando.value = true
            usuarioRepository.refrescar().onFailure { _mensajes.emit(it.message ?: "No se pudo actualizar la lista") }
            _cargando.value = false
        }
    }

}
