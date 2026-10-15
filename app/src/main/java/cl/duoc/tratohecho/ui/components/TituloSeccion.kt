package cl.duoc.tratohecho.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Título de bloque con una acción opcional a la derecha ("Ver todas")
@Composable
fun TituloSeccion(
    texto: String,
    modifier: Modifier = Modifier,
    accion: String? = null,
    onAccion: () -> Unit = {}
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.padding(top = 12.dp, bottom = 4.dp)) {
        Text(texto, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        if (accion != null) {
            TextButton(onClick = onAccion) {
                Text(accion, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
