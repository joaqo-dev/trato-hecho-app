package cl.duoc.tratohecho.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cl.duoc.tratohecho.data.local.entity.CotizacionEntity
import cl.duoc.tratohecho.domain.model.EstadoCotizacion
import cl.duoc.tratohecho.utils.Formatos

// Tarjeta de cotización pensada para escanear una lista:
//   Cliente ............................. Total
//   COT-0012 · 12 nov · Vendedor ........ [Estado]
//   (aviso cuando hay algo que hacer: vence pronto, en cola)
// El cliente va primero porque es como el vendedor recuerda cada cotización.
// Con mostrarTracker = true agrega el tracker del ciclo (cotización destacada).
@Composable
fun TarjetaCotizacion(
    cotizacion: CotizacionEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    mostrarVendedor: Boolean = false,
    mostrarTracker: Boolean = false
) {
    val interaccion = remember { MutableInteractionSource() }
    Card(
        onClick = onClick,
        interactionSource = interaccion,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
            .fillMaxWidth()
            .efectoPresion(interaccion)
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    cotizacion.clienteNombre,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = 12.dp)
                )
                Text(
                    Formatos.monto(cotizacion.total, cotizacion.moneda),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f).padding(end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        buildString {
                            append(cotizacion.numeroCompleto)
                            append(" · ")
                            append(Formatos.fechaCorta(cotizacion.fechaEnvio ?: cotizacion.fechaCreacion))
                            if (mostrarVendedor) append(" · ").append(cotizacion.vendedorNombre)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (cotizacion.pendienteSync) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Filled.CloudUpload,
                            contentDescription = "Pendiente de sincronizar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                ChipEstado(cotizacion.estado, enCola = cotizacion.envioPendiente, compacto = true)
            }

            val aviso = avisoDe(cotizacion)
            if (aviso != null) {
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Schedule, contentDescription = null, tint = aviso.color, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(aviso.texto, style = MaterialTheme.typography.labelMedium, color = aviso.color)
                }
            }

            if (mostrarTracker) {
                Spacer(Modifier.height(18.dp))
                TrackerCotizacion(cotizacion, mostrarFechas = false)
            }
        }
    }
}

private data class Aviso(val texto: String, val color: Color)

// Solo se avisa lo que pide una acción: una enviada que vence pronto o un envío en cola.
@Composable
private fun avisoDe(c: CotizacionEntity): Aviso? {
    val urgente = colorEstado(EstadoCotizacion.VENCIDA).texto
    val normal = MaterialTheme.colorScheme.onSurfaceVariant
    if (c.envioPendiente) return Aviso("Se enviará al recuperar la señal", urgente)
    if (c.estado != EstadoCotizacion.ENVIADA || c.fechaVencimiento == null) return null
    val dias = Formatos.diasHasta(c.fechaVencimiento)
    return when {
        dias < 0 -> null
        dias == 0 -> Aviso("Vence hoy", urgente)
        dias == 1 -> Aviso("Vence mañana", urgente)
        dias <= 3 -> Aviso("Vence en $dias días", urgente)
        else -> Aviso("Vence el ${Formatos.fechaCorta(c.fechaVencimiento)}", normal)
    }
}

// Texto pequeño gris que nombra un dato ("Cliente", "Total")
@Composable
fun Etiqueta(texto: String, modifier: Modifier = Modifier) {
    Text(
        texto,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(bottom = 2.dp)
    )
}
