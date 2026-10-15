package cl.duoc.tratohecho.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cl.duoc.tratohecho.domain.model.EstadoCotizacion
import cl.duoc.tratohecho.ui.theme.EstadoAceptada
import cl.duoc.tratohecho.ui.theme.EstadoAnulada
import cl.duoc.tratohecho.ui.theme.EstadoBorrador
import cl.duoc.tratohecho.ui.theme.EstadoEnviada
import cl.duoc.tratohecho.ui.theme.EstadoFacturada
import cl.duoc.tratohecho.ui.theme.EstadoRechazada
import cl.duoc.tratohecho.ui.theme.EstadoReemplazada
import cl.duoc.tratohecho.ui.theme.EstadoVencida

fun colorDeEstado(estado: EstadoCotizacion): Color = when (estado) {
    EstadoCotizacion.BORRADOR -> EstadoBorrador
    EstadoCotizacion.ENVIADA -> EstadoEnviada
    EstadoCotizacion.ACEPTADA -> EstadoAceptada
    EstadoCotizacion.RECHAZADA -> EstadoRechazada
    EstadoCotizacion.FACTURADA -> EstadoFacturada
    EstadoCotizacion.VENCIDA -> EstadoVencida
    EstadoCotizacion.ANULADA -> EstadoAnulada
    EstadoCotizacion.REEMPLAZADA -> EstadoReemplazada
}

// Etiqueta de estado; el color cambia con animación cuando la cotización cambia de estado
@Composable
fun ChipEstado(estado: EstadoCotizacion, modifier: Modifier = Modifier, enCola: Boolean = false) {
    val colorObjetivo = if (enCola) EstadoVencida else colorDeEstado(estado)
    val color by animateColorAsState(colorObjetivo, animationSpec = tween(600), label = "color_estado")
    Text(
        text = if (enCola) "En cola" else estado.etiqueta,
        color = color,
        style = MaterialTheme.typography.labelMedium,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}
