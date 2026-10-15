package cl.duoc.tratohecho.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.tratohecho.data.local.entity.ItemCotizacionEntity
import cl.duoc.tratohecho.domain.CalculadoraCotizacion
import cl.duoc.tratohecho.domain.model.Moneda
import cl.duoc.tratohecho.domain.model.TipoDescuento
import cl.duoc.tratohecho.utils.Formatos

// Ítem de una cotización (cantidad x precio, descuento, impuesto y subtotal)
@Composable
fun FilaItem(
    item: ItemCotizacionEntity,
    moneda: Moneda,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    acciones: @Composable () -> Unit = {}
) {
    Card(
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            disabledContentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 14.dp, top = 10.dp, bottom = 10.dp, end = 4.dp)) {
            Column(Modifier.weight(1f)) {
                Text(item.descripcion, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${Formatos.numero(item.cantidad)} ${item.unidad} × ${Formatos.monto(item.precioUnitario, moneda)}",
                    style = MaterialTheme.typography.bodySmall
                )
                val descuento = when {
                    item.descuentoValor <= 0 -> ""
                    item.descuentoTipo == TipoDescuento.PORCENTAJE -> " · Desc. ${Formatos.numero(item.descuentoValor)}%"
                    else -> " · Desc. ${Formatos.monto(item.descuentoValor, moneda)}"
                }
                Text(
                    "${item.impuestoNombre} ${Formatos.numero(item.impuestoPorcentaje)}%$descuento",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                Formatos.monto(CalculadoraCotizacion.subtotalItem(item), moneda),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            acciones()
        }
    }
}
