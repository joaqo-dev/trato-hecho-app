package cl.duoc.tratohecho.ui.screens.cotizaciones

import android.content.ActivityNotFoundException
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import cl.duoc.tratohecho.domain.HistorialCotizacion
import cl.duoc.tratohecho.nativo.ContactoHelper
import cl.duoc.tratohecho.ui.components.BloqueDetalle
import cl.duoc.tratohecho.ui.components.BotonCarga
import cl.duoc.tratohecho.ui.components.BotonSecundario
import cl.duoc.tratohecho.ui.components.Etiqueta
import cl.duoc.tratohecho.ui.components.FilaDetalle
import cl.duoc.tratohecho.ui.components.LineaTiempo
import cl.duoc.tratohecho.ui.components.TarjetaContacto
import cl.duoc.tratohecho.ui.components.TrackerCotizacion
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.tratohecho.domain.model.EstadoCotizacion
import cl.duoc.tratohecho.domain.model.Moneda
import cl.duoc.tratohecho.domain.model.Rol
import cl.duoc.tratohecho.domain.model.Sesion
import cl.duoc.tratohecho.ui.components.BarraConVolver
import cl.duoc.tratohecho.ui.components.ChipEstado
import cl.duoc.tratohecho.ui.components.DialogoConfirmacion
import cl.duoc.tratohecho.ui.components.FilaDato
import cl.duoc.tratohecho.ui.components.FilaItem
import cl.duoc.tratohecho.ui.components.TarjetaTotales
import cl.duoc.tratohecho.ui.components.TituloSeccion
import cl.duoc.tratohecho.utils.Formatos
import cl.duoc.tratohecho.viewmodel.CotizacionDetalleViewModel
import cl.duoc.tratohecho.viewmodel.EventoDetalleCotizacion

@Composable
fun CotizacionDetalleScreen(
    sesion: Sesion,
    mostrarMensaje: (String) -> Unit,
    onVolver: () -> Unit,
    onEditar: (String) -> Unit,
    onAbrirCotizacion: (String) -> Unit,
    viewModel: CotizacionDetalleViewModel = hiltViewModel()
) {
    val detalle by viewModel.detalle.collectAsStateWithLifecycle()
    val procesando by viewModel.procesando.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var confirmarAnular by remember { mutableStateOf(false) }
    var confirmarVersion by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                is EventoDetalleCotizacion.Mensaje -> mostrarMensaje(evento.texto)
                is EventoDetalleCotizacion.IrAFormulario -> onEditar(evento.id)
                is EventoDetalleCotizacion.AbrirVisor -> try {
                    context.startActivity(evento.intent)
                } catch (e: ActivityNotFoundException) {
                    mostrarMensaje("No hay una app para ver PDF instalada")
                }
                is EventoDetalleCotizacion.AbrirCorreo -> try {
                    context.startActivity(evento.intent)
                } catch (e: ActivityNotFoundException) {
                    mostrarMensaje("No hay una app de correo instalada en el teléfono")
                }
                EventoDetalleCotizacion.Volver -> onVolver()
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BarraConVolver("Detalle", onVolver) }
    ) { padding ->
        val actual = detalle
        if (actual == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        val cot = actual.datos.cotizacion
        val cliente = actual.cliente?.cliente
        val contacto = actual.cliente?.contactos?.find { it.id == cot.contactoId }
        // El administrador solo consulta. Vendedor y supervisor pueden modificar.
        val puedeModificar = sesion.rol != Rol.ADMINISTRADOR

        Column(Modifier.fillMaxSize().padding(padding)) {
            AnimatedVisibility(visible = procesando) { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp, bottom = 32.dp)
            ) {
                // ---------- Encabezado tipo "tracking" ----------
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Icon(Icons.Filled.Description, contentDescription = null)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Etiqueta("N° de cotización")
                        Text(cot.numeroCompleto, style = MaterialTheme.typography.titleLarge)
                    }
                    ChipEstado(cot.estado, enCola = cot.envioPendiente)
                }
                Spacer(Modifier.height(24.dp))
                TrackerCotizacion(cot)
                Spacer(Modifier.height(24.dp))

                // ---------- Acción principal según el estado ----------
                when {
                    puedeModificar && cot.estado == EstadoCotizacion.BORRADOR && !cot.envioPendiente ->
                        BotonCarga("Enviar al cliente", viewModel::enviar, procesando, icono = Icons.AutoMirrored.Filled.Send)
                    cot.estado == EstadoCotizacion.BORRADOR -> Unit
                    cot.estado != EstadoCotizacion.ANULADA ->
                        BotonCarga(
                            if (puedeModificar && cot.estado == EstadoCotizacion.ENVIADA) "Reenviar por correo" else "Enviar por correo",
                            viewModel::abrirCorreo,
                            cargando = false,
                            icono = Icons.AutoMirrored.Filled.Send
                        )
                }
                // Cotizaciones emitidas: ver el documento en el visor del sistema
                if (cot.estado != EstadoCotizacion.BORRADOR) {
                    Spacer(Modifier.height(10.dp))
                    BotonSecundario("Ver PDF", viewModel::verPdf, icono = Icons.Filled.PictureAsPdf)
                }

                // ---------- Datos ----------
                Spacer(Modifier.height(8.dp))
                BloqueDetalle("Datos de la cotización") {
                    FilaDetalle("Cliente", cot.clienteNombre)
                    if (cliente != null) FilaDetalle("RUT", cliente.rut)
                    FilaDetalle("Vendedor", cot.vendedorNombre)
                    FilaDetalle(
                        "Moneda",
                        if (cot.moneda == Moneda.CLP) "Peso chileno (CLP)"
                        else "${cot.moneda.name} · ${Formatos.monto(cot.tipoCambio)} (${cot.fechaTipoCambio ?: "-"})"
                    )
                    FilaDetalle("Validez", "${cot.diasValidez} días")
                    if (cot.fechaVencimiento != null) FilaDetalle("Vence", Formatos.fecha(cot.fechaVencimiento))
                    if (cot.pendienteSync) {
                        FilaDetalle("Sincronización", "Pendiente", colorValor = MaterialTheme.colorScheme.secondary)
                    }
                }

                // ---------- Contacto rápido ----------
                if (cliente != null) {
                    TituloSeccion("Contacto del cliente")
                    val nombre = contacto?.nombre ?: cliente.razonSocial
                    val correo = contacto?.correo ?: cliente.correo
                    val telefono = contacto?.telefono ?: cliente.telefono
                    TarjetaContacto(
                        nombre = nombre,
                        descripcion = contacto?.cargo?.let { "$it · ${cliente.razonSocial}" } ?: "Cliente",
                        onCorreo = {
                            if (!ContactoHelper.escribirCorreo(context, correo)) mostrarMensaje("No hay una app de correo instalada")
                        },
                        onLlamar = {
                            if (!ContactoHelper.llamar(context, telefono)) mostrarMensaje("Este teléfono no puede realizar llamadas")
                        }
                    )
                }

                // ---------- Ítems y totales ----------
                TituloSeccion("Ítems (${actual.datos.items.size})")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    actual.datos.items.sortedBy { it.orden }.forEach { item -> FilaItem(item, cot.moneda) }
                }
                Spacer(Modifier.height(10.dp))
                TarjetaTotales(actual.totales, cot.moneda, cot.tipoCambio)

                if (cot.notas.isNotBlank()) {
                    TituloSeccion("Notas")
                    Text(cot.notas, style = MaterialTheme.typography.bodyMedium)
                }

                // ---------- Historial ----------
                TituloSeccion("Historial")
                LineaTiempo(
                    eventos = HistorialCotizacion.eventos(cot),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(start = 18.dp, end = 18.dp, top = 18.dp)
                )

                // ---------- Otras acciones ----------
                val acciones = buildList {
                    if (puedeModificar && cot.estado == EstadoCotizacion.BORRADOR) add("editar")
                    if (puedeModificar && cot.estado.permiteNuevaVersion) add("version")
                    if (puedeModificar && cot.estado.permiteAnular) add("anular")
                    if (cot.cotizacionOrigenId != null) add("anterior")
                }
                if (acciones.isNotEmpty()) {
                    TituloSeccion("Más acciones")
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if ("editar" in acciones) BotonSecundario("Editar borrador", { onEditar(cot.id) }, icono = Icons.Filled.Edit)
                        if ("version" in acciones) BotonSecundario("Crear nueva versión", { confirmarVersion = true }, icono = Icons.Filled.ContentCopy)
                        if ("anterior" in acciones) BotonSecundario(
                            "Ver versión anterior", { onAbrirCotizacion(cot.cotizacionOrigenId!!) }, icono = Icons.Filled.History
                        )
                        if ("anular" in acciones) BotonSecundario(
                            if (cot.estado == EstadoCotizacion.BORRADOR) "Descartar borrador" else "Anular cotización",
                            { confirmarAnular = true },
                            icono = Icons.Filled.Block,
                            destructivo = true
                        )
                    }
                }
            }
        }
    }

    if (confirmarAnular) {
        DialogoConfirmacion(
            titulo = "Anular cotización",
            mensaje = "La cotización quedará anulada y no podrá enviarse ni aceptarse.",
            textoConfirmar = "Anular",
            onConfirmar = {
                confirmarAnular = false
                viewModel.anular()
            },
            onCancelar = { confirmarAnular = false }
        )
    }
    if (confirmarVersion) {
        DialogoConfirmacion(
            titulo = "Nueva versión",
            mensaje = "Se creará un borrador con los mismos datos para corregirlo. Esta versión quedará como reemplazada.",
            textoConfirmar = "Crear versión",
            onConfirmar = {
                confirmarVersion = false
                viewModel.crearNuevaVersion()
            },
            onCancelar = { confirmarVersion = false }
        )
    }
}
