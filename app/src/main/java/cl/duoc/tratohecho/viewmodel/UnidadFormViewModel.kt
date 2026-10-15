package cl.duoc.tratohecho.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tratohecho.data.local.SesionDataStore
import cl.duoc.tratohecho.data.local.entity.UnidadEntity
import cl.duoc.tratohecho.data.repository.CatalogoRepository
import cl.duoc.tratohecho.utils.Validaciones
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class UnidadFormUiState(
    val id: String = UUID.randomUUID().toString(),
    val esNuevo: Boolean = true,
    val nombre: String = "",
    val abreviatura: String = "",
    val errorNombre: String? = null,
    val errorAbreviatura: String? = null,
    val intentoGuardar: Boolean = false,
    val guardando: Boolean = false,
    val errorGeneral: String? = null
)

@HiltViewModel
class UnidadFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val catalogoRepository: CatalogoRepository,
    private val sesionDataStore: SesionDataStore
) : ViewModel() {

    private val _estado = MutableStateFlow(UnidadFormUiState())
    val estado: StateFlow<UnidadFormUiState> = _estado.asStateFlow()

    private val _guardado = MutableSharedFlow<String>()
    val guardado: SharedFlow<String> = _guardado.asSharedFlow()

    init {
        val id: String? = savedStateHandle["id"]
        if (id != null) {
            viewModelScope.launch {
                catalogoRepository.obtenerUnidad(id)?.let { u ->
                    _estado.value = UnidadFormUiState(id = u.id, esNuevo = false, nombre = u.nombre, abreviatura = u.abreviatura)
                }
            }
        }
    }

    private fun validarNombre(v: String) = Validaciones.largo(v, "El nombre", 2, 30)
    private fun validarAbreviatura(v: String) = Validaciones.largo(v, "La abreviatura", 1, 6)

    fun onNombreChange(v: String) = _estado.update {
        it.copy(nombre = v, errorNombre = if (it.intentoGuardar) validarNombre(v) else null, errorGeneral = null)
    }

    fun onAbreviaturaChange(v: String) = _estado.update {
        it.copy(abreviatura = v, errorAbreviatura = if (it.intentoGuardar) validarAbreviatura(v) else null, errorGeneral = null)
    }

    fun guardar() {
        val actual = _estado.value
        val errorNombre = validarNombre(actual.nombre)
        val errorAbreviatura = validarAbreviatura(actual.abreviatura)
        _estado.update { it.copy(errorNombre = errorNombre, errorAbreviatura = errorAbreviatura, intentoGuardar = true) }
        if (errorNombre != null || errorAbreviatura != null) return
        viewModelScope.launch {
            _estado.update { it.copy(guardando = true) }
            val unidad = UnidadEntity(
                id = actual.id,
                empresaId = sesionDataStore.obtenerSesion()?.empresaId ?: "",
                nombre = actual.nombre.trim(),
                abreviatura = actual.abreviatura.trim()
            )
            catalogoRepository.guardarUnidad(unidad)
                .onSuccess { _guardado.emit("Unidad guardada") }
                .onFailure { error -> _estado.update { it.copy(errorGeneral = error.message) } }
            _estado.update { it.copy(guardando = false) }
        }
    }
}
