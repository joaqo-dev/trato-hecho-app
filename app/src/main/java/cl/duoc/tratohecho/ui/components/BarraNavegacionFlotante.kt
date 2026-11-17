package cl.duoc.tratohecho.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            destinos.forEach { destino ->
                ItemBarra(destino, seleccionado = destino.ruta == rutaActual, onClick = { onSeleccionar(destino) })
            }
        }
    }
}

// Cada destino muestra siempre ícono y nombre: con cinco secciones, solo íconos obliga a recordar.
// La sección activa se marca con una píldora negra detrás del ícono (sin desplazar el resto).
@Composable
private fun ItemBarra(destino: DestinoBarra, seleccionado: Boolean, onClick: () -> Unit) {
    val interaccion = remember { MutableInteractionSource() }
    val fondo by animateColorAsState(
        if (seleccionado) MaterialTheme.colorScheme.primary else TratoTheme.colores.barraNavegacion,
        tween(DURACION_CORTA), label = "fondo_item"
    )
    val icono by animateColorAsState(
        if (seleccionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        tween(DURACION_CORTA), label = "icono_item"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(MaterialTheme.shapes.large)
            .selectable(
                selected = seleccionado,
                onClick = onClick,
                role = Role.Tab,
                interactionSource = interaccion,
                indication = null
            )
            .efectoPresion(interaccion)
            .widthIn(min = 60.dp)
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = 52.dp, height = 30.dp)
                .clip(Pildora)
                .background(fondo)
        ) {
            Icon(destino.icono, contentDescription = null, tint = icono, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(
            destino.titulo,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (seleccionado) FontWeight.SemiBold else FontWeight.Medium,
            color = if (seleccionado) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
