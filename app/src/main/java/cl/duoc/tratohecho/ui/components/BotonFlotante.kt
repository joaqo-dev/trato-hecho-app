package cl.duoc.tratohecho.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import cl.duoc.tratohecho.ui.theme.Pildora

// Acción principal de una lista: píldora negra flotante (una sola por pantalla)
@Composable
fun BotonFlotante(texto: String, icono: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interaccion = remember { MutableInteractionSource() }
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = { Icon(icono, contentDescription = null) },
        text = { Text(texto, style = MaterialTheme.typography.labelLarge) },
        shape = Pildora,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        interactionSource = interaccion,
        modifier = modifier.efectoPresion(interaccion)
    )
}
