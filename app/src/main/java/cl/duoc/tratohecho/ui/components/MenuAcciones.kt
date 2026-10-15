package cl.duoc.tratohecho.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

data class AccionMenu(
    val texto: String,
    val icono: ImageVector,
    val destructiva: Boolean = false,
    val onClick: () -> Unit
)

// Botón ⋮ que agrupa las acciones de una fila (editar, eliminar…).
// Mantiene las filas limpias: sin íconos de colores sueltos al lado de cada elemento.
@Composable
fun MenuAcciones(acciones: List<AccionMenu>, descripcion: String = "Más opciones", modifier: Modifier = Modifier) {
    var abierto by remember { mutableStateOf(false) }
    Box(modifier) {
        IconButton(onClick = { abierto = true }) {
            Icon(Icons.Filled.MoreVert, contentDescription = descripcion, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        DropdownMenu(
            expanded = abierto,
            onDismissRequest = { abierto = false },
            shape = MaterialTheme.shapes.medium,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ) {
            acciones.forEach { accion ->
                val color = if (accion.destructiva) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                DropdownMenuItem(
                    text = { Text(accion.texto, color = color) },
                    leadingIcon = { Icon(accion.icono, contentDescription = null, tint = color) },
                    onClick = {
                        abierto = false
                        accion.onClick()
                    }
                )
            }
        }
    }
}
