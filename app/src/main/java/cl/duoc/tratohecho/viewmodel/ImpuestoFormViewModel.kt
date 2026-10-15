package cl.duoc.tratohecho.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tratohecho.data.local.SesionDataStore
import cl.duoc.tratohecho.data.local.entity.ImpuestoEntity
import cl.duoc.tratohecho.data.repository.CatalogoRepository
import cl.duoc.tratohecho.utils.Formatos
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

data class ImpuestoFormUiState(
    val id: String = UUID.randomUUID().toString(),
    val esNuevo: Boolean = true,
    val nombre: String = "",
    val porcentaje: String = "",
    val errorNombre: String? = null,
    val errorPorcentaje: String? = null,
    val intentoGuardar: Boolean = false,
    val guardando: Boolean = false,
    val errorGeneral: String? = null
)

@HiltViewModel
class ImpuestoFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val catalogoRepository: CatalogoRepository,
    private val sesionDataStore: SesionDataStore
) : ViewModel() {

    private val _estado = MutableStateFlow(ImpuestoFormUiState())
    val estado: StateFlow<ImpuestoFormUiState> = _estado.asStateFlow()

    private val _guardado = MutableSharedFlow<String>()
    val guardado: SharedFlow<String> = _guardado.asSharedFlow()

    init {
        val id: String? = savedStateHandle["id"]
        if (id != null) {
            viewModelScope.launch {
                catalogoRepository.obtenerImpuesto(id)?.let { i ->
                    _estado.value = ImpuestoFormUiState(
                        id = i.id, esNuevo = false, nombre = i.nombre, porcentaje = Formatos.paraCampo(i.porcentaje)
                    )
                }
            }
        }
    }

    private fun validarNombre(v: String) = Validaciones.largo(v, "El nombre", 2, 30)
    private fun validarPorcentaje(v: String) = Validaciones.rango(v, "El porcentaje", 0.0, 100.0)

    fun onNombreChange(v: String) = _estado.update {
        it.copy(nombre = v, errorNombre = if (it.intentoGuardar) validarNombre(v) else null, errorGeneral = null)
    }

    fun onPorcentajeChange(v: String) = _estado.update {
        it.copy(porcentaje = v, errorPorcentaje = if (it.intentoGuardar) validarPorcentaje(v) else null, errorGeneral = null)
    }

    fun guardar() {
        val actual = _estado.value
        val errorNombre = validarNombre(actual.nombre)
        val errorPorcentaje = validarPorcentaje(actual.porcentaje)
        _estado.update { it.copy(errorNombre = errorNombre, errorPorcentaje = errorPorcentaje, intentoGuardar = true) }
        if (errorNombre != null || errorPorcentaje != null) return
        viewModelScope.launch {
            _estado.update { it.copy(guardando = true) }
            val impuesto = ImpuestoEntity(
                id = actual.id,
                empresaId = sesionDataStore.obtenerSesion()?.empresaId ?: "",
                nombre = actual.nombre.trim(),
                porcentaje = Validaciones.aNumero(actual.porcentaje)
            )
            catalogoRepository.guardarImpuesto(impuesto)
                .onSuccess { _guardado.emit("Impuesto guardado") }
                .onFailure { error -> _estado.update { it.copy(errorGeneral = error.message) } }
            _estado.update { it.copy(guardando = false) }
        }
    }
}
