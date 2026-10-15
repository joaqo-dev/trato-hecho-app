package cl.duoc.tratohecho.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import cl.duoc.tratohecho.ui.theme.DURACION_PRESION
import cl.duoc.tratohecho.ui.theme.EaseOutFuerte

// Retroalimentación al presionar: el elemento se achica un poco (0.97) mientras el dedo está encima.
// Así el usuario siente que la app "escuchó" el toque.
fun Modifier.efectoPresion(interactionSource: InteractionSource): Modifier = composed {
    val presionado by interactionSource.collectIsPressedAsState()
    val escala by animateFloatAsState(
        targetValue = if (presionado) 0.97f else 1f,
        animationSpec = tween(DURACION_PRESION, easing = EaseOutFuerte),
        label = "presion"
    )
    graphicsLayer {
        scaleX = escala
        scaleY = escala
    }
}
