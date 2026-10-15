package cl.duoc.tratohecho.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Discount
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.duoc.tratohecho.domain.model.Moneda
import cl.duoc.tratohecho.domain.model.TipoDescuento
import cl.duoc.tratohecho.ui.theme.DURACION_CORTA

// Descuento opcional y discreto: no forma parte del flujo principal.
// Plegado se ve solo un enlace; al tocarlo aparece el tipo (% o monto) y el valor.
@Composable
fun SeccionDescuento(
    textoEnlace: String,
    tipo: TipoDescuento,
    onTipoChange: (TipoDescuento) -> Unit,
    valor: String,
    onValorChange: (String) -> Unit,
    error: String?,
    moneda: Moneda,
    modifier: Modifier = Modifier
) {
    // Si ya tiene un descuento (al editar) parte abierto
    var abierto by rememberSaveable { mutableStateOf(valor.isNotBlank()) }

    AnimatedContent(
        targetState = abierto || error != null,
        transitionSpec = {
            (fadeIn(tween(DURACION_CORTA)) + expandVertically(tween(DURACION_CORTA))) togetherWith
                (fadeOut(tween(120)) + shrinkVertically(tween(DURACION_CORTA)))
        },
        label = "descuento",
        modifier = modifier.fillMaxWidth()
    ) { mostrar ->
        if (!mostrar) {
            TextButton(onClick = { abierto = true }) {
                Icon(Icons.Outlined.Discount, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(textoEnlace, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Column {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Descuento", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        onValorChange("")
                        abierto = false
                    }) {
                        Icon(Icons.Filled.Close, contentDescription = "Quitar descuento")
                    }
                }
                SelectorOpciones(
                    opciones = TipoDescuento.entries,
                    seleccionada = tipo,
                    textoDe = { if (it == TipoDescuento.PORCENTAJE) "Porcentaje (%)" else "Monto (${moneda.simbolo})" },
                    onSeleccionar = onTipoChange
                )
                Spacer(Modifier.height(8.dp))
                CampoTexto(
                    valor = valor,
                    onValorChange = onValorChange,
                    etiqueta = if (tipo == TipoDescuento.PORCENTAJE) "Porcentaje de descuento" else "Monto de descuento",
                    icono = Icons.Outlined.Discount,
                    error = error,
                    tipoTeclado = KeyboardType.Decimal
                )
            }
        }
    }
}
