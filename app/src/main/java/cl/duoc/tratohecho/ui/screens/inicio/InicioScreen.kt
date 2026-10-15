package cl.duoc.tratohecho.ui.screens.inicio

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.tratohecho.domain.model.EstadoCotizacion
import cl.duoc.tratohecho.domain.model.Indicadores
import cl.duoc.tratohecho.domain.model.Rol
import cl.duoc.tratohecho.domain.model.Sesion
import cl.duoc.tratohecho.ui.components.DialogoConfirmacion
import cl.duoc.tratohecho.ui.components.TituloSeccion
import cl.duoc.tratohecho.ui.components.colorDeEstado
import cl.duoc.tratohecho.utils.Formatos
import cl.duoc.tratohecho.viewmodel.InicioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioScreen(
    sesion: Sesion,
    onSincronizar: () -> Unit,
    onCerrarSesion: () -> Unit,
    onAbrirCotizaciones: () -> Unit,
    viewModel: InicioViewModel = hiltViewModel()
) {
    val indicadores by viewModel.indicadores.collectAsStateWithLifecycle()
    val pendientes by viewModel.pendientes.collectAsStateWithLifecycle()
    val sincronizando by viewModel.sincronizando.collectAsStateWithLifecycle()
    var confirmarSalida by remember { mutableStateOf(false) }

    // El ícono de sincronizar gira mientras se sincroniza
    val giro = rememberInfiniteTransition(label = "giro")
    val angulo by giro.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Restart),
        label = "angulo"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Hola, ${sesion.nombre.substringBefore(" ")}", maxLines = 1)
                        Text(
                            "${sesion.empresaNombre} · ${sesion.rol.etiqueta}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSincronizar, enabled = !sincronizando) {
                        Icon(
                            Icons.Filled.Sync,
                            contentDescription = "Sincronizar",
                            modifier = Modifier.rotate(if (sincronizando) angulo else 0f)
                        )
                    }
                    IconButton(onClick = { confirmarSalida = true }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                }
            )
        }
    ) { padding ->
        val datos = indicadores
        if (datos == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    if (sesion.rol == Rol.VENDEDOR) "Tus indicadores" else "Indicadores de la empresa",
                    style = MaterialTheme.typography.titleLarge
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TarjetaKpi(
                        titulo = "Cotizado este mes",
                        valor = datos.montoCotizadoMes,
                        formato = { Formatos.monto(it) },
                        icono = Icons.Filled.AttachMoney,
                        modifier = Modifier.weight(1f)
                    )
                    TarjetaKpi(
                        titulo = "Enviadas este mes",
                        valor = datos.cotizacionesDelMes.toDouble(),
                        formato = { it.toInt().toString() },
                        icono = Icons.Filled.Description,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TarjetaKpi(
                        titulo = "Tasa de aceptación",
                        valor = datos.tasaAceptacion,
                        formato = { Formatos.porcentaje(it) },
                        icono = Icons.Filled.ThumbUp,
                        progreso = datos.tasaAceptacion.toFloat(),
                        modifier = Modifier.weight(1f)
                    )
                    TarjetaKpi(
                        titulo = "Total facturado",
                        valor = datos.montoFacturado,
                        formato = { Formatos.monto(it) },
                        icono = Icons.AutoMirrored.Filled.ReceiptLong,
                        modifier = Modifier.weight(1f)
                    )
                }

                TarjetaPorEstado(datos)

                if (sesion.rol != Rol.VENDEDOR) {
                    TarjetaRanking(datos)
                }

                OutlinedButton(onClick = onAbrirCotizaciones, modifier = Modifier.fillMaxWidth()) {
                    Text("Ver todas las cotizaciones")
                }
            }
        }
    }

    if (confirmarSalida) {
        DialogoConfirmacion(
            titulo = "Cerrar sesión",
            mensaje = if (pendientes > 0) {
                "Tienes $pendientes cambio(s) sin sincronizar que se perderán. Conéctate a internet antes de salir si quieres conservarlos."
            } else {
                "¿Quieres cerrar tu sesión en este teléfono?"
            },
            textoConfirmar = "Cerrar sesión",
            onConfirmar = {
                confirmarSalida = false
                onCerrarSesion()
            },
            onCancelar = { confirmarSalida = false }
        )
    }
}

// Tarjeta de indicador: el número se anima desde 0 hasta su valor
@Composable
private fun TarjetaKpi(
    titulo: String,
    valor: Double,
    formato: (Double) -> String,
    icono: ImageVector,
    modifier: Modifier = Modifier,
    progreso: Float? = null
) {
    val animado by animateFloatAsState(valor.toFloat(), animationSpec = tween(900), label = "kpi")
    val progresoAnimado by animateFloatAsState(progreso ?: 0f, animationSpec = tween(900), label = "progreso")

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(Modifier.padding(14.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(titulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                formato(animado.toDouble()),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (progreso != null) {
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progresoAnimado },
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun TarjetaPorEstado(datos: Indicadores) {
    val estados = listOf(
        EstadoCotizacion.BORRADOR, EstadoCotizacion.ENVIADA, EstadoCotizacion.ACEPTADA,
        EstadoCotizacion.RECHAZADA, EstadoCotizacion.FACTURADA, EstadoCotizacion.VENCIDA, EstadoCotizacion.ANULADA
    )
    val maximo = (datos.porEstado.values.maxOrNull() ?: 1).coerceAtLeast(1)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TituloSeccion("Cotizaciones por estado")
            estados.forEach { estado ->
                val cantidad = datos.porEstado[estado] ?: 0
                val ancho by animateFloatAsState(cantidad.toFloat() / maximo, animationSpec = tween(800), label = "barra")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(estado.etiqueta, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(92.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(ancho)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(7.dp))
                                .background(colorDeEstado(estado))
                        )
                    }
                    Text(
                        cantidad.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.width(32.dp).padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaRanking(datos: Indicadores) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            TituloSeccion("Ranking de vendedores (ganado)")
            if (datos.rankingVendedores.isEmpty()) {
                Text("Aún no hay cotizaciones aceptadas", style = MaterialTheme.typography.bodyMedium)
            }
            datos.rankingVendedores.forEachIndexed { i, vendedor ->
                if (i > 0) HorizontalDivider(Modifier.padding(vertical = 6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = if (i == 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(vendedor.nombre, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "${vendedor.cantidadGanadas} cotización(es) ganada(s)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(Formatos.monto(vendedor.montoGanado), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
