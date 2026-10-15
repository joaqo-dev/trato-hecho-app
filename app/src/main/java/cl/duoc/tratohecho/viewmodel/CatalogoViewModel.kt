package cl.duoc.tratohecho.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tratohecho.data.local.entity.ImpuestoEntity
import cl.duoc.tratohecho.data.local.entity.ProductoEntity
import cl.duoc.tratohecho.data.local.entity.UnidadEntity
import cl.duoc.tratohecho.data.repository.CatalogoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// Producto con el nombre de su unidad e impuesto para mostrarlo en la lista
data class ProductoConDetalle(
    val producto: ProductoEntity,
    val unidad: String,
    val impuesto: String
)

@HiltViewModel
class CatalogoViewModel @Inject constructor(
    private val catalogoRepository: CatalogoRepository
) : ViewModel() {

    val unidades: StateFlow<List<UnidadEntity>> = catalogoRepository.observarUnidades()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val impuestos: StateFlow<List<ImpuestoEntity>> = catalogoRepository.observarImpuestos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val productos: StateFlow<List<ProductoConDetalle>> = combine(
        catalogoRepository.observarProductos(),
        catalogoRepository.observarUnidades(),
        catalogoRepository.observarImpuestos()
    ) { productos, unidades, impuestos ->
        productos.map { p ->
            ProductoConDetalle(
                producto = p,
                unidad = unidades.find { it.id == p.unidadId }?.abreviatura ?: "-",
                impuesto = impuestos.find { it.id == p.impuestoId }?.let { "${it.nombre} ${it.porcentaje.toInt()}%" } ?: "-"
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _mensajes = MutableSharedFlow<String>()
    val mensajes: SharedFlow<String> = _mensajes.asSharedFlow()

    fun eliminarProducto(id: String) = eliminar("Producto eliminado") { catalogoRepository.eliminarProducto(id) }
    fun eliminarUnidad(id: String) = eliminar("Unidad eliminada") { catalogoRepository.eliminarUnidad(id) }
    fun eliminarImpuesto(id: String) = eliminar("Impuesto eliminado") { catalogoRepository.eliminarImpuesto(id) }

    private fun eliminar(mensajeExito: String, accion: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            accion()
                .onSuccess { _mensajes.emit(mensajeExito) }
                .onFailure { _mensajes.emit(it.message ?: "No se pudo eliminar") }
        }
    }
}
