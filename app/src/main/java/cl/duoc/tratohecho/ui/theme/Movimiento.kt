package cl.duoc.tratohecho.ui.theme

import androidx.compose.animation.core.CubicBezierEasing

// Curvas de animación (más marcadas que las de serie): responden de inmediato y frenan suave.
val EaseOutFuerte = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)       // entradas y respuestas a un toque
val EaseInOutFuerte = CubicBezierEasing(0.77f, 0f, 0.175f, 1f)   // elementos que se mueven en pantalla

// Duraciones en milisegundos
const val DURACION_PRESION = 120
const val DURACION_CORTA = 200
const val DURACION_MEDIA = 280
const val DURACION_TRACKER = 700
