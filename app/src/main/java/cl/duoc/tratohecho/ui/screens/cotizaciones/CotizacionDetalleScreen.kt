package cl.duoc.tratohecho.ui.screens.cotizaciones

import android.content.ActivityNotFoundException
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Science
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
        topBar = { BarraConVolver(detalle?.datos?.cotizacion?.numeroCompleto ?: "Cotización", onVolver) }
    ) { padding ->
        val actual = detalle
        if (actual == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        val cot = actual.datos.cotizacion
        val contacto = actual.cliente?.contactos?.find { it.id == cot.contactoId }
        // El administrador solo consulta. Vendedor y supervisor pueden modificar.
        val puedeModificar = sesion.rol != Rol.ADMINISTRADOR

        Column(Modifier.fillMaxSize().padding(padding)) {
            AnimatedVisibility(visible = procesando) { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // ---------- Encabezado ----------
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(Modifier.padding(16.dp).fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(cot.clienteNombre, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                            ChipEstado(cot.estado, enCola = cot.envioPendiente)
                        }
                        if (contacto != null) FilaDato(Icons.Filled.Person, "${contacto.nombre} · ${contacto.correo}")
                        else FilaDato(Icons.Filled.Email, actual.cliente?.cliente?.correo ?: "-")
                        FilaDato(Icons.Filled.Business, "Vendedor: ${cot.vendedorNombre}")
                        FilaDato(Icons.Filled.CalendarMonth, "Creada: ${Formatos.fecha(cot.fechaCreacion)} · Enviada: ${Formatos.fecha(cot.fechaEnvio)}")
                        FilaDato(Icons.Filled.EventBusy, "Validez: ${cot.diasValidez} días · Vence: ${Formatos.fecha(cot.fechaVencimiento)}")
                        if (cot.moneda != Moneda.CLP) {
                            FilaDato(
                                Icons.Filled.CurrencyExchange,
                                "1 ${cot.moneda.name} = ${Formatos.monto(cot.tipoCambio)} (${cot.fechaTipoCambio ?: "-"})"
                            )
                        }
                    }
                }

                MensajeDeEstado(
                    estado = cot.estado,
                    enCola = cot.envioPendiente,
                    fechaRespuesta = cot.fechaRespuesta
                )

                // ---------- Ítems y totales ----------
                TituloSeccion("Ítems (${actual.datos.items.size})")
                actual.datos.items.sortedBy { it.orden }.forEach { item ->
                    FilaItem(item, cot.moneda)
                }
                TarjetaTotales(actual.totales, cot.moneda, cot.tipoCambio)

                if (cot.notas.isNotBlank()) {
                    TituloSeccion("Notas")
                    Text(cot.notas, style = MaterialTheme.typography.bodyMedium)
                }

                cot.cotizacionOrigenId?.let { origenId ->
                    TextButton(onClick = { onAbrirCotizacion(origenId) }) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null)
                        Text("  Ver versión anterior")
                    }
                }

                // ---------- Acciones según estado y rol ----------
                TituloSeccion("Acciones")
                if (puedeModificar && cot.estado == EstadoCotizacion.BORRADOR) {
                    if (!cot.envioPendiente) {
                        Boton("Enviar al cliente", Icons.AutoMirrored.Filled.Send, principal = true, habilitado = !procesando) { viewModel.enviar() }
                    }
                    Boton("Editar borrador", Icons.Filled.Edit) { onEditar(cot.id) }
                }
                if (cot.estado != EstadoCotizacion.BORRADOR && cot.estado != EstadoCotizacion.ANULADA) {
                    Boton(
                        if (puedeModificar && cot.estado == EstadoCotizacion.ENVIADA) "Reenviar por correo" else "Compartir PDF",
                        Icons.Filled.PictureAsPdf
                    ) { viewModel.abrirCorreo() }
                }
                if (puedeModificar && cot.estado.permiteNuevaVersion) {
                    Boton("Crear nueva versión", Icons.Filled.ContentCopy) { confirmarVersion = true }
                }
                if (puedeModificar && cot.estado == EstadoCotizacion.ENVIADA) {
                    // Solo para la demo: la aceptación real ocurre en la página pública de Trato Hecho
                    Boton("Simular respuesta del cliente (demo)", Icons.Filled.Science, habilitado = !procesando) { viewModel.simularRespuesta() }
                }
                if (puedeModificar && cot.estado.permiteAnular) {
                    Boton(
                        if (cot.estado == EstadoCotizacion.BORRADOR) "Descartar borrador" else "Anular cotización",
                        Icons.Filled.Block
                    ) { confirmarAnular = true }
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

@Composable
private fun MensajeDeEstado(estado: EstadoCotizacion, enCola: Boolean, fechaRespuesta: Long?) {
    val (icono, texto) = when {
        enCola -> Icons.Filled.HourglassTop to "En cola de envío: se enviará automáticamente al recuperar la señal."
        estado == EstadoCotizacion.ENVIADA -> Icons.Filled.HourglassTop to "Esperando la respuesta del cliente en la página pública."
        estado == EstadoCotizacion.ACEPTADA -> Icons.Filled.Person to "El cliente aceptó la cotización el ${Formatos.fecha(fechaRespuesta)}."
        estado == EstadoCotizacion.RECHAZADA -> Icons.Filled.Person to "El cliente rechazó la cotización el ${Formatos.fecha(fechaRespuesta)}."
        estado == EstadoCotizacion.VENCIDA -> Icons.Filled.EventBusy to "Venció sin respuesta. Puedes crear una nueva versión."
        estado == EstadoCotizacion.REEMPLAZADA -> Icons.Filled.ContentCopy to "Esta versión fue reemplazada por una más nueva."
        else -> return
    }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp).fillMaxWidth()) {
            Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Text(
                "  $texto",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun Boton(
    texto: String,
    icono: ImageVector,
    principal: Boolean = false,
    habilitado: Boolean = true,
    onClick: () -> Unit
) {
    if (principal) {
        Button(onClick = onClick, enabled = habilitado, modifier = Modifier.fillMaxWidth()) {
            Icon(icono, contentDescription = null)
            Text("  $texto")
        }
    } else {
        OutlinedButton(onClick = onClick, enabled = habilitado, modifier = Modifier.fillMaxWidth()) {
            Icon(icono, contentDescription = null)
            Text("  $texto")
        }
    }
}
