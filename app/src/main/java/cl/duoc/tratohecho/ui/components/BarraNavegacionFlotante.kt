package cl.duoc.tratohecho.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cl.duoc.tratohecho.ui.theme.DURACION_CORTA
import cl.duoc.tratohecho.ui.theme.Pildora
import cl.duoc.tratohecho.ui.theme.TratoTheme

// Espacio que deben dejar al final las pantallas con barra, para que su contenido
// y su botón flotante no queden tapados por la píldora
val ESPACIO_BARRA_NAVEGACION = 96.dp

data class DestinoBarra(val ruta: String, val titulo: String, val icono: ImageVector)

// Barra de navegación inferior en forma de píldora flotante.
// La sección activa se marca con una píldora negra que muestra ícono + nombre.
@Composable
fun BarraNavegacionFlotante(
    destinos: List<DestinoBarra>,
    rutaActual: String?,
    onSeleccionar: (DestinoBarra) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = Pildora,
        color = TratoTheme.colores.barraNavegacion,
        shadowElevation = 12.dp,
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(6.dp)
        ) {
            destinos.forEach { destino ->
                ItemBarra(destino, seleccionado = destino.ruta == rutaActual, onClick = { onSeleccionar(destino) })
            }
        }
    }
}

@Composable
private fun ItemBarra(destino: DestinoBarra, seleccionado: Boolean, onClick: () -> Unit) {
    val interaccion = remember { MutableInteractionSource() }
    val fondo by animateColorAsState(
        if (seleccionado) MaterialTheme.colorScheme.primary else TratoTheme.colores.barraNavegacion,
        tween(DURACION_CORTA), label = "fondo_item"
    )
    val contenido by animateColorAsState(
        if (seleccionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        tween(DURACION_CORTA), label = "contenido_item"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(Pildora)
            .background(fondo)
            .selectable(
                selected = seleccionado,
                onClick = onClick,
                role = Role.Tab,
                interactionSource = interaccion,
                indication = null
            )
            .efectoPresion(interaccion)
            .defaultMinSize(minWidth = 52.dp, minHeight = 52.dp)
            .padding(horizontal = 15.dp)
    ) {
        Icon(destino.icono, contentDescription = destino.titulo, tint = contenido)
        // El nombre aparece solo en la sección activa, deslizándose con un resorte suave
        AnimatedVisibility(
            visible = seleccionado,
            enter = fadeIn(tween(DURACION_CORTA)) + expandHorizontally(spring(stiffness = Spring.StiffnessMediumLow)),
            exit = fadeOut(tween(120)) + shrinkHorizontally(spring(stiffness = Spring.StiffnessMediumLow))
        ) {
            Row {
                Spacer(Modifier.width(8.dp))
                Text(destino.titulo, style = MaterialTheme.typography.labelLarge, color = contenido, maxLines = 1)
            }
        }
    }
}
