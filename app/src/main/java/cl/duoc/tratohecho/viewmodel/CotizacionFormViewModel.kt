package cl.duoc.tratohecho.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tratohecho.data.local.SesionDataStore
import cl.duoc.tratohecho.data.local.entity.ClienteConContactos
import cl.duoc.tratohecho.data.local.entity.CotizacionEntity
import cl.duoc.tratohecho.data.local.entity.ImpuestoEntity
import cl.duoc.tratohecho.data.local.entity.ItemCotizacionEntity
import cl.duoc.tratohecho.data.local.entity.ProductoEntity
import cl.duoc.tratohecho.data.local.entity.UnidadEntity
import cl.duoc.tratohecho.data.repository.CatalogoRepository
import cl.duoc.tratohecho.data.repository.ClienteRepository
import cl.duoc.tratohecho.data.repository.CotizacionRepository
import cl.duoc.tratohecho.data.repository.IndicadoresEconomicosRepository
import cl.duoc.tratohecho.data.repository.ResultadoEnvio
import cl.duoc.tratohecho.data.sync.GestorSincronizacion
import cl.duoc.tratohecho.domain.CalculadoraCotizacion
import cl.duoc.tratohecho.domain.model.Moneda
import cl.duoc.tratohecho.domain.model.TipoDescuento
import cl.duoc.tratohecho.domain.model.TotalesCotizacion
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

data class CotizacionErrores(
    val cliente: String? = null,
    val items: String? = null,
    val diasValidez: String? = null,
    val descuentoGlobal: String? = null,
    val tipoCambio: String? = null,
    val notas: String? = null
) {
    val hayErrores: Boolean get() = listOf(cliente, items, diasValidez, descuentoGlobal, tipoCambio, notas).any { it != null }
}

data class ItemErrores(
    val producto: String? = null,
    val descripcion: String? = null,
    val cantidad: String? = null,
    val precio: String? = null,
    val descuento: String? = null,
    val impuesto: String? = null
) {
    val hayErrores: Boolean get() = listOf(producto, descripcion, cantidad, precio, descuento, impuesto).any { it != null }
}

// Estado del diálogo para agregar o editar un ítem
data class ItemFormState(
    val id: String = UUID.randomUUID().toString(),
    val esNuevo: Boolean = true,
    val productoId: String? = null,
    val descripcion: String = "",
    val unidad: String = "",
    val cantidad: String = "1",
    val precio: String = "",
    val descuentoTipo: TipoDescuento = TipoDescuento.PORCENTAJE,
    val descuento: String = "",
    val impuestoId: String? = null,
    val errores: ItemErrores = ItemErrores(),
    val intentoGuardar: Boolean = false
)

data class CotizacionFormUiState(
    val cargando: Boolean = true,
    val id: String = UUID.randomUUID().toString(),
    val esNueva: Boolean = true,
    val base: CotizacionEntity? = null,   // datos que no se editan (número, versión, vendedor, fechas)
    val clienteId: String? = null,
    val contactoId: String? = null,
    val moneda: Moneda = Moneda.CLP,
    val tipoCambio: Double = 1.0,
    val fechaTipoCambio: String? = null,
    val tipoCambioDesdeCache: Boolean = false,
    val cargandoTipoCambio: Boolean = false,
    val diasValidez: String = "15",
    val descuentoGlobalTipo: TipoDescuento = TipoDescuento.PORCENTAJE,
    val descuentoGlobal: String = "",
    val notas: String = "",
    val items: List<ItemCotizacionEntity> = emptyList(),
    val totales: TotalesCotizacion = TotalesCotizacion(),
    val errores: CotizacionErrores = CotizacionErrores(),
    val intentoGuardar: Boolean = false,
    val guardando: Boolean = false,
    val dialogoItem: ItemFormState? = null
)

sealed class EventoFormCotizacion {
    data class Guardada(val mensaje: String) : EventoFormCotizacion()
    data class Enviada(val id: String) : EventoFormCotizacion()
    data class EnCola(val id: String, val mensaje: String) : EventoFormCotizacion()
    data class Mensaje(val texto: String) : EventoFormCotizacion()
}

@HiltViewModel
class CotizacionFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cotizacionRepository: CotizacionRepository,
    private val indicadoresRepository: IndicadoresEconomicosRepository,
    private val sesionDataStore: SesionDataStore,
    private val gestorSincronizacion: GestorSincronizacion,
    clienteRepository: ClienteRepository,
    catalogoRepository: CatalogoRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(CotizacionFormUiState())
    val estado: StateFlow<CotizacionFormUiState> = _estado.asStateFlow()

    private val _eventos = MutableSharedFlow<EventoFormCotizacion>()
    val eventos: SharedFlow<EventoFormCotizacion> = _eventos.asSharedFlow()

    val clientes: StateFlow<List<ClienteConContactos>> = clienteRepository.observarClientes()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val productos: StateFlow<List<ProductoEntity>> = catalogoRepository.observarProductos()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val unidades: StateFlow<List<UnidadEntity>> = catalogoRepository.observarUnidades()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val impuestos: StateFlow<List<ImpuestoEntity>> = catalogoRepository.observarImpuestos()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        val id: String? = savedStateHandle["id"]
        viewModelScope.launch {
            val existente = id?.let { cotizacionRepository.obtenerCotizacion(it) }
            if (existente != null) {
                val c = existente.cotizacion
                _estado.value = recalcular(
                    CotizacionFormUiState(
                        cargando = false,
                        id = c.id,
                        esNueva = false,
                        base = c,
                        clienteId = c.clienteId,
                        contactoId = c.contactoId,
                        moneda = c.moneda,
                        tipoCambio = c.tipoCambio,
                        fechaTipoCambio = c.fechaTipoCambio,
                        diasValidez = c.diasValidez.toString(),
                        descuentoGlobalTipo = c.descuentoGlobalTipo,
                        descuentoGlobal = if (c.descuentoGlobalValor > 0) Formatos.paraCampo(c.descuentoGlobalValor) else "",
                        notas = c.notas,
                        items = existente.items.sortedBy { it.orden }
                    )
                )
            } else {
                _estado.update { it.copy(cargando = false) }
            }
        }
    }

    // ---------- Cálculo y validación (se ejecutan en cada cambio) ----------

    private fun recalcular(e: CotizacionFormUiState): CotizacionFormUiState {
        val tipoGlobal = e.descuentoGlobalTipo
        val totales = CalculadoraCotizacion.calcular(e.items, tipoGlobal, Validaciones.aNumero(e.descuentoGlobal), e.moneda)
        val conTotales = e.copy(totales = totales)
        return if (conTotales.intentoGuardar) conTotales.copy(errores = validar(conTotales)) else conTotales
    }

    private fun actualizar(cambio: (CotizacionFormUiState) -> CotizacionFormUiState) =
        _estado.update { recalcular(cambio(it)) }

    private fun validar(e: CotizacionFormUiState) = CotizacionErrores(
        cliente = if (e.clienteId == null) "Selecciona un cliente" else null,
        items = if (e.items.isEmpty()) "Agrega al menos un ítem" else null,
        diasValidez = Validaciones.rango(e.diasValidez, "La validez", 1.0, 90.0),
        descuentoGlobal = Validaciones.descuento(
            e.descuentoGlobal, e.descuentoGlobalTipo == TipoDescuento.PORCENTAJE, e.totales.subtotal
        ),
        tipoCambio = if (e.moneda != Moneda.CLP && e.tipoCambio <= 1.0) "No hay valor de ${e.moneda.name} disponible" else null,
        notas = if (e.notas.length > 500) "Las notas no pueden superar 500 caracteres" else null
    )

    // ---------- Datos generales ----------

    fun onClienteChange(cliente: ClienteConContactos) = actualizar {
        it.copy(clienteId = cliente.cliente.id, contactoId = cliente.contactos.firstOrNull()?.id)
    }

    fun onContactoChange(contactoId: String?) = actualizar { it.copy(contactoId = contactoId) }
    fun onDiasValidezChange(v: String) = actualizar { it.copy(diasValidez = v.filter { c -> c.isDigit() }.take(2)) }
    fun onDescuentoGlobalTipoChange(tipo: TipoDescuento) = actualizar { it.copy(descuentoGlobalTipo = tipo) }
    fun onDescuentoGlobalChange(v: String) = actualizar { it.copy(descuentoGlobal = v) }
    fun onNotasChange(v: String) = actualizar { it.copy(notas = v) }

    // Al cambiar la moneda se obtiene el valor del dólar/UF y se convierten los precios de los ítems
    fun onMonedaChange(moneda: Moneda) {
        val actual = _estado.value
        if (moneda == actual.moneda) return
        cargarTipoCambio(moneda)
    }

    fun actualizarTipoCambio() = cargarTipoCambio(_estado.value.moneda)

    private fun cargarTipoCambio(moneda: Moneda) {
        viewModelScope.launch {
            _estado.update { it.copy(cargandoTipoCambio = true) }
            indicadoresRepository.obtener(moneda)
                .onSuccess { indicador ->
                    actualizar { e ->
                        val itemsConvertidos = e.items.map { item ->
                            val precioClp = item.precioUnitario * e.tipoCambio
                            val descuentoClp = if (item.descuentoTipo == TipoDescuento.MONTO) item.descuentoValor * e.tipoCambio else item.descuentoValor
                            item.copy(
                                precioUnitario = CalculadoraCotizacion.convertirDesdeClp(precioClp, moneda, indicador.valor),
                                descuentoValor = if (item.descuentoTipo == TipoDescuento.MONTO)
                                    CalculadoraCotizacion.convertirDesdeClp(descuentoClp, moneda, indicador.valor)
                                else descuentoClp
                            )
                        }
                        e.copy(
                            moneda = moneda,
                            tipoCambio = indicador.valor,
                            fechaTipoCambio = indicador.fecha.ifBlank { null },
                            tipoCambioDesdeCache = indicador.desdeCache,
                            cargandoTipoCambio = false,
                            items = itemsConvertidos,
                            // Un descuento global en monto no tiene sentido al cambiar de moneda
                            descuentoGlobal = if (e.descuentoGlobalTipo == TipoDescuento.MONTO) "" else e.descuentoGlobal
                        )
                    }
                }
                .onFailure { error ->
                    _estado.update { it.copy(cargandoTipoCambio = false) }
                    _eventos.emit(EventoFormCotizacion.Mensaje(error.message ?: "No se pudo obtener el valor de la moneda"))
                }
        }
    }

    // ---------- Ítems ----------

    fun abrirNuevoItem() = _estado.update { it.copy(dialogoItem = ItemFormState()) }

    fun editarItem(item: ItemCotizacionEntity) = _estado.update {
        it.copy(
            dialogoItem = ItemFormState(
                id = item.id,
                esNuevo = false,
                productoId = item.productoId,
                descripcion = item.descripcion,
                unidad = item.unidad,
                cantidad = Formatos.paraCampo(item.cantidad),
                precio = Formatos.paraCampo(item.precioUnitario),
                descuentoTipo = item.descuentoTipo,
                descuento = if (item.descuentoValor > 0) Formatos.paraCampo(item.descuentoValor) else "",
                impuestoId = impuestos.value.find { imp -> imp.nombre == item.impuestoNombre && imp.porcentaje == item.impuestoPorcentaje }?.id
            )
        )
    }

    fun cerrarItem() = _estado.update { it.copy(dialogoItem = null) }

    fun eliminarItem(item: ItemCotizacionEntity) = actualizar { e -> e.copy(items = e.items.filter { it.id != item.id }) }

    private fun actualizarItem(cambio: (ItemFormState) -> ItemFormState) = _estado.update { actual ->
        val dialogo = actual.dialogoItem ?: return@update actual
        val nuevo = cambio(dialogo)
        actual.copy(dialogoItem = if (nuevo.intentoGuardar) nuevo.copy(errores = validarItem(nuevo)) else nuevo)
    }

    // Al elegir un producto se completan descripción, unidad, precio (convertido) e impuesto
    fun onItemProductoChange(producto: ProductoEntity) {
        val e = _estado.value
        val unidad = unidades.value.find { it.id == producto.unidadId }?.abreviatura ?: "un"
        val precio = CalculadoraCotizacion.convertirDesdeClp(producto.precio, e.moneda, e.tipoCambio)
        actualizarItem {
            it.copy(
                productoId = producto.id,
                descripcion = producto.nombre,
                unidad = unidad,
                precio = Formatos.paraCampo(precio),
                impuestoId = producto.impuestoId
            )
        }
    }

    fun onItemDescripcionChange(v: String) = actualizarItem { it.copy(descripcion = v) }
    fun onItemCantidadChange(v: String) = actualizarItem { it.copy(cantidad = v) }
    fun onItemPrecioChange(v: String) = actualizarItem { it.copy(precio = v) }
    fun onItemDescuentoTipoChange(v: TipoDescuento) = actualizarItem { it.copy(descuentoTipo = v) }
    fun onItemDescuentoChange(v: String) = actualizarItem { it.copy(descuento = v) }
    fun onItemImpuestoChange(v: ImpuestoEntity) = actualizarItem { it.copy(impuestoId = v.id) }

    private fun validarItem(i: ItemFormState): ItemErrores {
        val bruto = Validaciones.aNumero(i.cantidad) * Validaciones.aNumero(i.precio)
        return ItemErrores(
            producto = if (i.productoId == null) "Selecciona un producto del catálogo" else null,
            descripcion = Validaciones.largo(i.descripcion, "La descripción", 3, 80),
            cantidad = Validaciones.numeroPositivo(i.cantidad, "La cantidad"),
            precio = Validaciones.numeroPositivo(i.precio, "El precio"),
            descuento = Validaciones.descuento(i.descuento, i.descuentoTipo == TipoDescuento.PORCENTAJE, bruto),
            impuesto = if (i.impuestoId == null) "Selecciona un impuesto" else null
        )
    }

    fun guardarItem() {
        val dialogo = _estado.value.dialogoItem ?: return
        val errores = validarItem(dialogo)
        if (errores.hayErrores) {
            _estado.update { it.copy(dialogoItem = dialogo.copy(errores = errores, intentoGuardar = true)) }
            return
        }
        val impuesto = impuestos.value.find { it.id == dialogo.impuestoId }
        val item = ItemCotizacionEntity(
            id = dialogo.id,
            cotizacionId = _estado.value.id,
            productoId = dialogo.productoId,
            descripcion = dialogo.descripcion.trim(),
            unidad = dialogo.unidad,
            cantidad = Validaciones.aNumero(dialogo.cantidad),
            precioUnitario = Validaciones.aNumero(dialogo.precio),
            descuentoTipo = dialogo.descuentoTipo,
            descuentoValor = Validaciones.aNumero(dialogo.descuento),
            impuestoNombre = impuesto?.nombre ?: "IVA",
            impuestoPorcentaje = impuesto?.porcentaje ?: 19.0
        )
        actualizar { e ->
            val existe = e.items.any { it.id == item.id }
            val lista = if (existe) e.items.map { if (it.id == item.id) item else it } else e.items + item
            e.copy(items = lista, dialogoItem = null)
        }
    }

    // ---------- Guardar y enviar ----------

    private suspend fun guardarLocal(): String? {
        val e = _estado.value
        val errores = validar(e)
        _estado.update { it.copy(errores = errores, intentoGuardar = true) }
        if (errores.hayErrores) {
            _eventos.emit(EventoFormCotizacion.Mensaje("Revisa los campos marcados en rojo"))
            return null
        }
        val sesion = sesionDataStore.obtenerSesion() ?: return null
        val cliente = clientes.value.find { it.cliente.id == e.clienteId }!!
        val base = e.base
        val cotizacion = CotizacionEntity(
            id = e.id,
            empresaId = base?.empresaId ?: sesion.empresaId,
            numero = base?.numero ?: cotizacionRepository.numeroTemporal(),
            version = base?.version ?: 1,
            cotizacionOrigenId = base?.cotizacionOrigenId,
            clienteId = cliente.cliente.id,
            clienteNombre = cliente.cliente.razonSocial,
            contactoId = e.contactoId,
            // Si el supervisor edita, la cotización sigue siendo del vendedor original
            vendedorId = base?.vendedorId ?: sesion.usuarioId,
            vendedorNombre = base?.vendedorNombre ?: sesion.nombre,
            moneda = e.moneda,
            tipoCambio = if (e.moneda == Moneda.CLP) 1.0 else e.tipoCambio,
            fechaTipoCambio = if (e.moneda == Moneda.CLP) null else e.fechaTipoCambio,
            diasValidez = e.diasValidez.toInt(),
            descuentoGlobalTipo = e.descuentoGlobalTipo,
            descuentoGlobalValor = Validaciones.aNumero(e.descuentoGlobal),
            notas = e.notas.trim(),
            fechaCreacion = base?.fechaCreacion ?: System.currentTimeMillis()
        )
        cotizacionRepository.guardarBorrador(cotizacion, e.items)
        return cotizacion.id
    }

    fun guardarBorrador() {
        viewModelScope.launch {
            _estado.update { it.copy(guardando = true) }
            val id = guardarLocal()
            _estado.update { it.copy(guardando = false) }
            if (id != null) {
                gestorSincronizacion.sincronizarEnSegundoPlano()
                _eventos.emit(EventoFormCotizacion.Guardada("Borrador guardado"))
            }
        }
    }

    fun guardarYEnviar() {
        viewModelScope.launch {
            _estado.update { it.copy(guardando = true) }
            val id = guardarLocal()
            if (id == null) {
                _estado.update { it.copy(guardando = false) }
                return@launch
            }
            val resultado = cotizacionRepository.enviar(id)
            _estado.update { it.copy(guardando = false) }
            when (resultado) {
                is ResultadoEnvio.Enviada -> _eventos.emit(EventoFormCotizacion.Enviada(id))
                is ResultadoEnvio.EnCola -> _eventos.emit(EventoFormCotizacion.EnCola(id, resultado.motivo))
            }
        }
    }
}
