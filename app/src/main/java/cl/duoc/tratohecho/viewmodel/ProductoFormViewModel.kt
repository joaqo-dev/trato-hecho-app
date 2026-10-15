package cl.duoc.tratohecho.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tratohecho.data.local.SesionDataStore
import cl.duoc.tratohecho.data.local.entity.ImpuestoEntity
import cl.duoc.tratohecho.data.local.entity.ProductoEntity
import cl.duoc.tratohecho.data.local.entity.UnidadEntity
import cl.duoc.tratohecho.data.repository.CatalogoRepository
import cl.duoc.tratohecho.utils.Formatos
import cl.duoc.tratohecho.utils.Validaciones
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ProductoErrores(
    val nombre: String? = null,
    val descripcion: String? = null,
    val precio: String? = null,
    val unidad: String? = null,
    val impuesto: String? = null
) {
    val hayErrores: Boolean get() = listOf(nombre, descripcion, precio, unidad, impuesto).any { it != null }
}

data class ProductoFormUiState(
    val id: String = UUID.randomUUID().toString(),
    val esNuevo: Boolean = true,
    val nombre: String = "",
    val descripcion: String = "",
    val precio: String = "",
    val unidadId: String? = null,
    val impuestoId: String? = null,
    val errores: ProductoErrores = ProductoErrores(),
    val intentoGuardar: Boolean = false,
    val guardando: Boolean = false,
    val errorGeneral: String? = null
)

@HiltViewModel
class ProductoFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val catalogoRepository: CatalogoRepository,
    private val sesionDataStore: SesionDataStore
) : ViewModel() {

    private val _estado = MutableStateFlow(ProductoFormUiState())
    val estado: StateFlow<ProductoFormUiState> = _estado.asStateFlow()

    val unidades: StateFlow<List<UnidadEntity>> = catalogoRepository.observarUnidades()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val impuestos: StateFlow<List<ImpuestoEntity>> = catalogoRepository.observarImpuestos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _guardado = MutableSharedFlow<String>()
    val guardado: SharedFlow<String> = _guardado.asSharedFlow()

    init {
        val id: String? = savedStateHandle["id"]
        if (id != null) {
            viewModelScope.launch {
                catalogoRepository.obtenerProducto(id)?.let { p ->
                    _estado.value = ProductoFormUiState(
                        id = p.id, esNuevo = false, nombre = p.nombre, descripcion = p.descripcion,
                        precio = Formatos.paraCampo(p.precio), unidadId = p.unidadId, impuestoId = p.impuestoId
                    )
                }
            }
        }
    }

    private fun actualizar(cambio: (ProductoFormUiState) -> ProductoFormUiState) = _estado.update { actual ->
        val nuevo = cambio(actual).copy(errorGeneral = null)
        if (nuevo.intentoGuardar) nuevo.copy(errores = validar(nuevo)) else nuevo
    }

    fun onNombreChange(v: String) = actualizar { it.copy(nombre = v) }
    fun onDescripcionChange(v: String) = actualizar { it.copy(descripcion = v) }
    fun onPrecioChange(v: String) = actualizar { it.copy(precio = v.filter { c -> c.isDigit() }) }
    fun onUnidadChange(unidad: UnidadEntity) = actualizar { it.copy(unidadId = unidad.id) }
    fun onImpuestoChange(impuesto: ImpuestoEntity) = actualizar { it.copy(impuestoId = impuesto.id) }

    private fun validar(e: ProductoFormUiState) = ProductoErrores(
        nombre = Validaciones.largo(e.nombre, "El nombre", 3, 60),
        descripcion = if (e.descripcion.length > 120) "La descripción no puede superar 120 caracteres" else null,
        precio = Validaciones.numeroPositivo(e.precio, "El precio"),
        unidad = if (e.unidadId == null) "Selecciona una unidad" else null,
        impuesto = if (e.impuestoId == null) "Selecciona un impuesto" else null
    )

    fun guardar() {
        val actual = _estado.value
        val errores = validar(actual)
        _estado.update { it.copy(errores = errores, intentoGuardar = true) }
        if (errores.hayErrores) return
        viewModelScope.launch {
            _estado.update { it.copy(guardando = true) }
            val producto = ProductoEntity(
                id = actual.id,
                empresaId = sesionDataStore.obtenerSesion()?.empresaId ?: "",
                nombre = actual.nombre.trim(),
                descripcion = actual.descripcion.trim(),
                precio = Validaciones.aNumero(actual.precio),
                unidadId = actual.unidadId!!,
                impuestoId = actual.impuestoId!!
            )
            catalogoRepository.guardarProducto(producto)
                .onSuccess { _guardado.emit("Producto guardado") }
                .onFailure { error -> _estado.update { it.copy(errorGeneral = error.message) } }
            _estado.update { it.copy(guardando = false) }
        }
    }
}
