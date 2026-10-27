package cl.duoc.tratohecho.viewmodel

import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tratohecho.data.local.entity.ClienteConContactos
import cl.duoc.tratohecho.data.local.entity.CotizacionConItems
import cl.duoc.tratohecho.data.local.entity.EmpresaEntity
import cl.duoc.tratohecho.data.repository.ClienteRepository
import cl.duoc.tratohecho.data.repository.CotizacionRepository
import cl.duoc.tratohecho.data.repository.EmpresaRepository
import cl.duoc.tratohecho.data.repository.ResultadoEnvio
import cl.duoc.tratohecho.data.sync.GestorSincronizacion
import cl.duoc.tratohecho.domain.CalculadoraCotizacion
import cl.duoc.tratohecho.domain.model.TotalesCotizacion
import cl.duoc.tratohecho.nativo.CompartirHelper
import cl.duoc.tratohecho.nativo.GeneradorPdf
import cl.duoc.tratohecho.utils.Formatos
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class DetalleCotizacionUi(
    val datos: CotizacionConItems,
    val cliente: ClienteConContactos?,
    val empresa: EmpresaEntity?,
    val totales: TotalesCotizacion
)

sealed class EventoDetalleCotizacion {
    data class Mensaje(val texto: String) : EventoDetalleCotizacion()
    data class AbrirCorreo(val intent: Intent) : EventoDetalleCotizacion()
    data class AbrirVisor(val intent: Intent) : EventoDetalleCotizacion()
    data class IrAFormulario(val id: String) : EventoDetalleCotizacion()
    data object Volver : EventoDetalleCotizacion()
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class CotizacionDetalleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cotizacionRepository: CotizacionRepository,
    clienteRepository: ClienteRepository,
    empresaRepository: EmpresaRepository,
    private val gestorSincronizacion: GestorSincronizacion,
    private val generadorPdf: GeneradorPdf,
    private val compartirHelper: CompartirHelper
) : ViewModel() {

    private val cotizacionId: String = checkNotNull(savedStateHandle["id"])
    private var correoAutomatico: Boolean = savedStateHandle["abrirCorreo"] ?: false

    private val cotizacion = cotizacionRepository.observarCotizacion(cotizacionId)

    val detalle: StateFlow<DetalleCotizacionUi?> = combine(
        cotizacion,
        cotizacion.flatMapLatest { c -> c?.let { clienteRepository.observarCliente(it.cotizacion.clienteId) } ?: flowOf(null) },
        empresaRepository.observarEmpresa()
    ) { datos, cliente, empresa ->
        datos?.let {
            DetalleCotizacionUi(
                datos = it,
                cliente = cliente,
                empresa = empresa,
                totales = CalculadoraCotizacion.calcular(
                    it.items, it.cotizacion.descuentoGlobalTipo, it.cotizacion.descuentoGlobalValor, it.cotizacion.moneda
                )
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _procesando = MutableStateFlow(false)
    val procesando: StateFlow<Boolean> = _procesando.asStateFlow()

    private val _eventos = MutableSharedFlow<EventoDetalleCotizacion>()
    val eventos: SharedFlow<EventoDetalleCotizacion> = _eventos.asSharedFlow()

    init {
        // Al llegar desde "Enviar" o desde la notificación se abre el correo automáticamente
        if (correoAutomatico) {
            viewModelScope.launch {
                detalle.filterNotNull().first()
                correoAutomatico = false
                savedStateHandle["abrirCorreo"] = false
                abrirCorreo()
            }
        }
    }

    fun enviar() {
        viewModelScope.launch {
            _procesando.value = true
            when (val resultado = cotizacionRepository.enviar(cotizacionId)) {
                is ResultadoEnvio.Enviada -> {
                    _eventos.emit(EventoDetalleCotizacion.Mensaje("Cotización ${resultado.cotizacion.numeroCompleto} enviada"))
                    abrirCorreo()
                }
                is ResultadoEnvio.EnCola -> _eventos.emit(EventoDetalleCotizacion.Mensaje(resultado.motivo))
            }
            _procesando.value = false
        }
    }

    // Genera el PDF y prepara el correo para el contacto (o el cliente si no hay contacto)
    fun abrirCorreo() {
        val actual = detalle.value ?: return
        viewModelScope.launch {
            val cot = actual.datos.cotizacion
            val contacto = actual.cliente?.contactos?.find { it.id == cot.contactoId }
            val destinatario = contacto?.correo ?: actual.cliente?.cliente?.correo ?: ""
            val archivo = withContext(Dispatchers.IO) {
                generadorPdf.generar(actual.datos, actual.empresa, actual.cliente?.cliente, contacto)
            }
            val saludo = contacto?.nombre?.substringBefore(" ") ?: cot.clienteNombre
            val mensaje = """
                Hola $saludo:

                Adjuntamos la cotización ${cot.numeroCompleto} por un total de ${Formatos.monto(cot.total, cot.moneda)}.
                La propuesta es válida por ${cot.diasValidez} días.

                Quedamos atentos a tus comentarios.

                ${cot.vendedorNombre}
                ${actual.empresa?.razonSocial ?: ""}
            """.trimIndent()
            val intent = compartirHelper.crearIntentCorreo(
                archivo = archivo,
                destinatario = destinatario,
                asunto = "Cotización ${cot.numeroCompleto} - ${actual.empresa?.razonSocial ?: ""}",
                mensaje = mensaje
            )
            _eventos.emit(EventoDetalleCotizacion.AbrirCorreo(intent))
        }
    }

    // Genera el PDF y lo abre con el visor de archivos del teléfono
    fun verPdf() {
        val actual = detalle.value ?: return
        viewModelScope.launch {
            val cot = actual.datos.cotizacion
            val contacto = actual.cliente?.contactos?.find { it.id == cot.contactoId }
            val archivo = withContext(Dispatchers.IO) {
                generadorPdf.generar(actual.datos, actual.empresa, actual.cliente?.cliente, contacto)
            }
            _eventos.emit(EventoDetalleCotizacion.AbrirVisor(compartirHelper.crearIntentVer(archivo)))
        }
    }

    fun crearNuevaVersion() {
        viewModelScope.launch {
            val nuevaId = cotizacionRepository.crearNuevaVersion(cotizacionId) ?: return@launch
            gestorSincronizacion.sincronizarEnSegundoPlano()
            _eventos.emit(EventoDetalleCotizacion.IrAFormulario(nuevaId))
        }
    }

    fun anular() {
        viewModelScope.launch {
            val eliminada = cotizacionRepository.anular(cotizacionId)
            gestorSincronizacion.sincronizarEnSegundoPlano()
            if (eliminada) {
                _eventos.emit(EventoDetalleCotizacion.Mensaje("Borrador eliminado"))
                _eventos.emit(EventoDetalleCotizacion.Volver)
            } else {
                _eventos.emit(EventoDetalleCotizacion.Mensaje("Cotización anulada"))
            }
        }
    }
}
