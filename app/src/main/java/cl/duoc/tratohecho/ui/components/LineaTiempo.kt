package cl.duoc.tratohecho.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import cl.duoc.tratohecho.domain.EventoHistorial
import cl.duoc.tratohecho.domain.TipoEvento
import cl.duoc.tratohecho.domain.model.EstadoCotizacion
import cl.duoc.tratohecho.utils.Formatos

// Historial de la cotización: hora a la izquierda, punto con ícono y descripción a la derecha,
// unidos por una línea punteada (como el seguimiento de un envío)
@Composable
fun LineaTiempo(eventos: List<EventoHistorial>, modifier: Modifier = Modifier) {
    Column(modifier) {
        eventos.forEachIndexed { indice, evento ->
            FilaEvento(evento, esPrimero = indice == 0, esUltimo = indice == eventos.lastIndex)
        }
    }
}

@Composable
private fun FilaEvento(evento: EventoHistorial, esPrimero: Boolean, esUltimo: Boolean) {
    val colorLinea = MaterialTheme.colorScheme.outline
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Column(Modifier.width(64.dp).padding(top = 2.dp, bottom = 18.dp)) {
            Text(
                if (evento.fecha != null) Formatos.hora(evento.fecha) else "—",
                style = MaterialTheme.typography.titleSmall
            )
            if (evento.fecha != null) {
                Text(
                    Formatos.fechaCorta(evento.fecha),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        // Columna central: línea punteada + punto
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .width(36.dp)
                .fillMaxHeight()
                .drawBehind {
                    val x = size.width / 2
                    val desde = if (esPrimero) 12.dp.toPx() else 0f
                    val hasta = if (esUltimo) 12.dp.toPx() else size.height
                    if (hasta > desde) {
                        drawLine(
                            colorLinea, Offset(x, desde), Offset(x, hasta), 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                        )
                    }
                }
        ) {
            PuntoEvento(evento.tipo)
        }
        Column(
            Modifier
                .weight(1f)
                .padding(top = 2.dp, bottom = 18.dp)
        ) {
            Text(evento.titulo, style = MaterialTheme.typography.titleSmall)
            Text(
                evento.detalle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PuntoEvento(tipo: TipoEvento) {
    val (fondo, contenido, icono) = when (tipo) {
        TipoEvento.HECHO -> Triple(MaterialTheme.colorScheme.onSurface, MaterialTheme.colorScheme.surface, Icons.Filled.Check)
        TipoEvento.PENDIENTE -> colorEstado(EstadoCotizacion.BORRADOR, enCola = true).let { Triple(it.fondo, it.texto, Icons.Filled.Schedule) }
        TipoEvento.ALERTA -> colorEstado(EstadoCotizacion.VENCIDA).let { Triple(it.fondo, it.texto, Icons.Filled.PriorityHigh) }
        TipoEvento.ERROR -> colorEstado(EstadoCotizacion.RECHAZADA).let { Triple(it.fondo, it.texto, Icons.Filled.Close) }
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(fondo)
    ) {
        Icon(icono, contentDescription = null, tint = contenido, modifier = Modifier.size(14.dp))
    }
}
