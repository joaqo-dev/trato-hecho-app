package cl.duoc.tratohecho.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cl.duoc.tratohecho.ui.theme.DURACION_CORTA
import cl.duoc.tratohecho.ui.theme.Pildora

// Botón principal (píldora negra) que muestra un loader mientras se procesa la acción
@Composable
fun BotonCarga(
    texto: String,
    onClick: () -> Unit,
    cargando: Boolean,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
    habilitado: Boolean = true
) {
    val interaccion = remember { MutableInteractionSource() }
    Button(
        onClick = onClick,
        enabled = habilitado && !cargando,
        shape = Pildora,
        interactionSource = interaccion,
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = if (cargando) 1f else 0.3f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .efectoPresion(interaccion)
    ) {
        AnimatedContent(
            targetState = cargando,
            transitionSpec = { fadeIn(tween(DURACION_CORTA)) togetherWith fadeOut(tween(DURACION_CORTA)) },
            label = "boton_carga"
        ) { estaCargando ->
            if (estaCargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                ContenidoBoton(texto, icono)
            }
        }
    }
}

// Botón secundario (píldora con borde) para acciones de menor prioridad
@Composable
fun BotonSecundario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
    habilitado: Boolean = true,
    destructivo: Boolean = false
) {
    val interaccion = remember { MutableInteractionSource() }
    val color = if (destructivo) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    OutlinedButton(
        onClick = onClick,
        enabled = habilitado,
        shape = Pildora,
        interactionSource = interaccion,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .efectoPresion(interaccion)
    ) {
        ContenidoBoton(texto, icono)
    }
}

@Composable
private fun ContenidoBoton(texto: String, icono: ImageVector?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (icono != null) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
        }
        Text(texto, style = MaterialTheme.typography.labelLarge)
    }
}
