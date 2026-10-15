package cl.duoc.tratohecho.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cl.duoc.tratohecho.domain.model.EstadoCotizacion
import cl.duoc.tratohecho.ui.theme.ColorEstado
import cl.duoc.tratohecho.ui.theme.DURACION_MEDIA
import cl.duoc.tratohecho.ui.theme.Pildora
import cl.duoc.tratohecho.ui.theme.TratoTheme

@Composable
fun colorEstado(estado: EstadoCotizacion, enCola: Boolean = false): ColorEstado =
    if (enCola) TratoTheme.colores.enCola else TratoTheme.colores.estados.getValue(estado)

// Color principal de un estado (para barras y nodos del tracker)
@Composable
fun colorDeEstado(estado: EstadoCotizacion): Color = colorEstado(estado).texto

// Píldora de estado con fondo suave; el color cambia con animación cuando la cotización cambia de estado
@Composable
fun ChipEstado(estado: EstadoCotizacion, modifier: Modifier = Modifier, enCola: Boolean = false) {
    val colores = colorEstado(estado, enCola)
    val fondo by animateColorAsState(colores.fondo, tween(DURACION_MEDIA), label = "fondo_estado")
    val texto by animateColorAsState(colores.texto, tween(DURACION_MEDIA), label = "texto_estado")
    Text(
        text = if (enCola) "En cola" else estado.etiqueta,
        color = texto,
        style = MaterialTheme.typography.labelMedium,
        modifier = modifier
            .clip(Pildora)
            .background(fondo)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    )
}
