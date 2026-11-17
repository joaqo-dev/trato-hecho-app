package cl.duoc.tratohecho.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun DialogoConfirmacion(
    titulo: String,
    mensaje: String,
    textoConfirmar: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    textoCancelar: String = "Cancelar",
    destructivo: Boolean = false   // la acción borra o anula algo: el botón se pinta en rojo
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = { Text(mensaje) },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                Text(
                    textoConfirmar,
                    color = if (destructivo) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text(textoCancelar, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    )
}
