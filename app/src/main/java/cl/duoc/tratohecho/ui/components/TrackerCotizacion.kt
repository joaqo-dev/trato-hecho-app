package cl.duoc.tratohecho.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cl.duoc.tratohecho.data.local.entity.CotizacionEntity
import cl.duoc.tratohecho.domain.EstadoPaso
import cl.duoc.tratohecho.domain.PasoTracker
import cl.duoc.tratohecho.domain.ProgresoCotizacion
import cl.duoc.tratohecho.domain.model.EstadoCotizacion
import cl.duoc.tratohecho.ui.theme.DURACION_TRACKER
import cl.duoc.tratohecho.ui.theme.EaseInOutFuerte
import cl.duoc.tratohecho.utils.Formatos


// Pieza firma de la app: el ciclo de la cotización como un tracker de envío.
// La línea se "llena" con animación la primera vez que aparece.
@Composable
fun TrackerCotizacion(
    cotizacion: CotizacionEntity,
    modifier: Modifier = Modifier,
    mostrarFechas: Boolean = true,
    compacto: Boolean = false     // versión mini para las tarjetas de lista: solo nodos y línea
) {
    val tamanoNodo = if (compacto) 18.dp else 28.dp
    val progreso = ProgresoCotizacion.calcular(cotizacion)

    var iniciado by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { iniciado = true }
    val avance by animateFloatAsState(
        targetValue = if (iniciado) progreso.avance else 0f,
        animationSpec = tween(DURACION_TRACKER, easing = EaseInOutFuerte),
        label = "avance_tracker"
    )

    val colorPista = MaterialTheme.colorScheme.surfaceContainerHighest
    val colorLinea = MaterialTheme.colorScheme.onSurface
    val n = progreso.pasos.size

    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (progreso.inactiva) 0.45f else 1f)
            .drawBehind {
                // La línea va del centro del primer nodo al centro del último
                val y = tamanoNodo.toPx() / 2
                val inicio = size.width / (2 * n)
                val fin = size.width - inicio
                val grosor = (if (compacto) 3.dp else 4.dp).toPx()
                drawLine(colorPista, Offset(inicio, y), Offset(fin, y), grosor, StrokeCap.Round)
                drawLine(colorLinea, Offset(inicio, y), Offset(inicio + (fin - inicio) * avance, y), grosor, StrokeCap.Round)
            }
    ) {
        progreso.pasos.forEach { paso ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                NodoTracker(paso, cotizacion.estado, tamanoNodo)
                if (compacto) return@Column
                Spacer(Modifier.height(8.dp))
                Text(
                    paso.titulo,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (paso.estado == EstadoPaso.PENDIENTE) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                if (mostrarFechas && paso.fecha != null) {
                    Text(
                        "${Formatos.fechaCorta(paso.fecha)} ${Formatos.hora(paso.fecha)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun NodoTracker(paso: PasoTracker, estado: EstadoCotizacion, tamano: Dp) {
    val esquema = MaterialTheme.colorScheme
    val icono = tamano * 0.55f
    Box(Modifier.size(tamano), contentAlignment = Alignment.Center) {
        when (paso.estado) {
            EstadoPaso.COMPLETADO -> CirculoNodo(esquema.surface, esquema.onSurface, tamano) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = esquema.onSurface, modifier = Modifier.size(icono))
            }
            EstadoPaso.EN_CURSO -> {
                val color = colorEstado(estado, enCola = true)
                CirculoNodo(color.fondo, MaterialTheme.colorScheme.secondary, tamano) {
                    Icon(Icons.Filled.Schedule, contentDescription = null, tint = color.texto, modifier = Modifier.size(icono))
                }
            }
            EstadoPaso.FALLIDO -> {
                val color = colorEstado(estado)
                CirculoNodo(color.fondo, color.texto, tamano) {
                    Icon(
                        if (estado == EstadoCotizacion.VENCIDA) Icons.Filled.Schedule else Icons.Filled.Close,
                        contentDescription = null,
                        tint = color.texto,
                        modifier = Modifier.size(icono)
                    )
                }
            }
            EstadoPaso.PENDIENTE -> Box(
                Modifier
                    .size(tamano * 0.64f)
                    .clip(CircleShape)
                    .background(esquema.surfaceContainerHighest)
            )
        }
    }
}

@Composable
private fun CirculoNodo(fondo: Color, borde: Color, tamano: Dp, contenido: @Composable () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(tamano)
            .clip(CircleShape)
            .background(fondo)
            .border(if (tamano < 24.dp) 2.dp else 2.5.dp, borde, CircleShape)
    ) { contenido() }
}
