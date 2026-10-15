package cl.duoc.tratohecho.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cl.duoc.tratohecho.data.local.entity.CotizacionEntity
import cl.duoc.tratohecho.utils.Formatos

// Tarjeta de cotización para las listas
@Composable
fun TarjetaCotizacion(
    cotizacion: CotizacionEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    mostrarVendedor: Boolean = false
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
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
                Spacer(Modifier.weight(1f))
                ChipEstado(cotizacion.estado, enCola = cotizacion.envioPendiente)
            }
            Spacer(Modifier.padding(top = 4.dp))
            Text(
                cotizacion.clienteNombre,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (mostrarVendedor) "${cotizacion.vendedorNombre} · ${Formatos.fecha(cotizacion.fechaEnvio ?: cotizacion.fechaCreacion)}"
                    else Formatos.fecha(cotizacion.fechaEnvio ?: cotizacion.fechaCreacion),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    Formatos.monto(cotizacion.total, cotizacion.moneda),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
