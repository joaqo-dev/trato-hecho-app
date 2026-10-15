package cl.duoc.tratohecho.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// Botones segmentados para elegir una opción (moneda, tipo de descuento, rol)
@Composable
fun <T> SelectorOpciones(
    opciones: List<T>,
    seleccionada: T,
    textoDe: (T) -> String,
    onSeleccionar: (T) -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        opciones.forEachIndexed { indice, opcion ->
            SegmentedButton(
                selected = opcion == seleccionada,
                onClick = { onSeleccionar(opcion) },
                enabled = habilitado,
                shape = SegmentedButtonDefaults.itemShape(index = indice, count = opciones.size),
                label = { Text(textoDe(opcion)) }
            )
        }
    }
}
