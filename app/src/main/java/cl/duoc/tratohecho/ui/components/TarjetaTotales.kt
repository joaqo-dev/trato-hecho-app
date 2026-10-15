package cl.duoc.tratohecho.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import cl.duoc.tratohecho.ui.theme.DURACION_CORTA
import cl.duoc.tratohecho.ui.theme.EaseOutFuerte
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
import androidx.compose.ui.unit.dp
import cl.duoc.tratohecho.domain.model.Moneda
import cl.duoc.tratohecho.domain.model.TotalesCotizacion
import cl.duoc.tratohecho.utils.Formatos

// Resumen de montos. El total se anima cada vez que cambia.
@Composable
fun TarjetaTotales(totales: TotalesCotizacion, moneda: Moneda, tipoCambio: Double, modifier: Modifier = Modifier) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            FilaMonto("Subtotal", Formatos.monto(totales.subtotal, moneda))
            if (totales.descuentoGlobal > 0) FilaMonto("Descuento", "- " + Formatos.monto(totales.descuentoGlobal, moneda))
            FilaMonto("Neto", Formatos.monto(totales.neto, moneda))
            FilaMonto("Impuestos", Formatos.monto(totales.impuestos, moneda))
            HorizontalDivider(Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Total", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                AnimatedContent(
                    targetState = Formatos.monto(totales.total, moneda),
                    transitionSpec = {
                        (slideInVertically(tween(DURACION_CORTA, easing = EaseOutFuerte)) { it / 2 } + fadeIn(tween(DURACION_CORTA))) togetherWith
                            (slideOutVertically(tween(DURACION_CORTA)) { -it / 2 } + fadeOut(tween(120)))
                    },
                    label = "total"
                ) { texto ->
                    Text(texto, style = MaterialTheme.typography.headlineSmall.copy(fontFeatureSettings = "tnum"))
                }
            }
            if (moneda != Moneda.CLP && tipoCambio > 1.0) {
                Text(
                    "≈ ${Formatos.monto(totales.total * tipoCambio)} (1 ${moneda.name} = ${Formatos.monto(tipoCambio)})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun FilaMonto(etiqueta: String, valor: String) {
    Row(Modifier.padding(vertical = 2.dp)) {
        Text(
            etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(valor, style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"))
    }
}
