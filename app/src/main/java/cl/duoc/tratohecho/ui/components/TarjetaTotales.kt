package cl.duoc.tratohecho.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.tratohecho.domain.model.Moneda
import cl.duoc.tratohecho.domain.model.TotalesCotizacion
import cl.duoc.tratohecho.utils.Formatos

// Resumen de montos. El total se anima cada vez que cambia.
@Composable
fun TarjetaTotales(totales: TotalesCotizacion, moneda: Moneda, tipoCambio: Double, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            FilaMonto("Subtotal", Formatos.monto(totales.subtotal, moneda))
            if (totales.descuentoGlobal > 0) FilaMonto("Descuento", "- " + Formatos.monto(totales.descuentoGlobal, moneda))
            FilaMonto("Neto", Formatos.monto(totales.neto, moneda))
            FilaMonto("Impuestos", Formatos.monto(totales.impuestos, moneda))
            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("TOTAL", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                AnimatedContent(
                    targetState = Formatos.monto(totales.total, moneda),
                    transitionSpec = { (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut()) },
                    label = "total"
                ) { texto ->
                    Text(texto, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
            if (moneda != Moneda.CLP && tipoCambio > 1.0) {
                Text(
                    "≈ ${Formatos.monto(totales.total * tipoCambio)} (1 ${moneda.name} = ${Formatos.monto(tipoCambio)})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun FilaMonto(etiqueta: String, valor: String) {
    Row(Modifier.padding(vertical = 2.dp)) {
        Text(etiqueta, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(valor, style = MaterialTheme.typography.bodyMedium)
    }
}
