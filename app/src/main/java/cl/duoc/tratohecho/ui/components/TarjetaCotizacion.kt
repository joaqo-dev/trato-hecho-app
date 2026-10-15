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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cl.duoc.tratohecho.data.local.entity.CotizacionEntity
import cl.duoc.tratohecho.utils.Formatos

// Tarjeta de cotización: número, estado, cliente y total en dos columnas (como un envío).
// Con mostrarTracker = true agrega el tracker del ciclo (se usa para la cotización en curso).
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
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Etiqueta("N° de cotización")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(cotizacion.numeroCompleto, style = MaterialTheme.typography.titleMedium)
                        if (cotizacion.pendienteSync) {
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                Icons.Filled.CloudUpload,
                                contentDescription = "Pendiente de sincronizar",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                ChipEstado(cotizacion.estado, enCola = cotizacion.envioPendiente)
            }
            Spacer(Modifier.height(16.dp))
            Row {
                Column(Modifier.weight(1f).padding(end = 12.dp)) {
                    Etiqueta("Cliente")
                    Text(
                        cotizacion.clienteNombre,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        if (mostrarVendedor) "${cotizacion.vendedorNombre} · ${Formatos.fecha(cotizacion.fechaEnvio ?: cotizacion.fechaCreacion)}"
                        else Formatos.fecha(cotizacion.fechaEnvio ?: cotizacion.fechaCreacion),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Etiqueta("Total")
                    Text(
                        Formatos.monto(cotizacion.total, cotizacion.moneda),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            // El estado se lee como un tracker, no solo como etiqueta
            if (mostrarTracker) {
                Spacer(Modifier.height(20.dp))
                TrackerCotizacion(cotizacion, mostrarFechas = false)
            } else {
                Spacer(Modifier.height(14.dp))
                TrackerCotizacion(cotizacion, mostrarFechas = false, compacto = true)
            }
        }
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
