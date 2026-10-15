package cl.duoc.tratohecho.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import cl.duoc.tratohecho.domain.model.EstadoCotizacion

private val EsquemaClaro = lightColorScheme(
    primary = Negro,
    onPrimary = Blanco,
    primaryContainer = Gris150,
    onPrimaryContainer = Negro,
    secondary = Ambar,
    onSecondary = Color(0xFF1F1300),
    secondaryContainer = AmbarClaro,
    onSecondaryContainer = AmbarOscuro,
    tertiary = Color(0xFF15803D),
    onTertiary = Blanco,
    tertiaryContainer = Color(0xFFE3F6EA),
    onTertiaryContainer = Color(0xFF14532D),
    background = Blanco,
    onBackground = Negro,
    surface = Blanco,
    onSurface = Negro,
    surfaceVariant = Gris100,
    onSurfaceVariant = Gris500,
    surfaceContainerLowest = Blanco,
    surfaceContainerLow = Gris50,
    surfaceContainer = Gris100,
    surfaceContainerHigh = Gris150,
    surfaceContainerHighest = Gris200,
    outline = Gris300,
    outlineVariant = Gris150,
    error = Color(0xFFD92D20),
    onError = Blanco,
    errorContainer = Color(0xFFFEECEB),
    onErrorContainer = Color(0xFF8A1C14),
    inverseSurface = Negro,
    inverseOnSurface = Blanco
)

private val EsquemaOscuro = darkColorScheme(
    primary = Humo,
    onPrimary = Negro,
    primaryContainer = Carbon3,
    onPrimaryContainer = Humo,
    secondary = AmbarNoche,
    onSecondary = Color(0xFF2E1A00),
    secondaryContainer = Color(0xFF4A2E08),
    onSecondaryContainer = AmbarClaro,
    tertiary = Color(0xFF86EFAC),
    onTertiary = Color(0xFF052E16),
    tertiaryContainer = Color(0xFF052E16),
    onTertiaryContainer = Color(0xFF86EFAC),
    background = Carbon,
    onBackground = Humo,
    surface = Carbon,
    onSurface = Humo,
    surfaceVariant = Carbon2,
    onSurfaceVariant = HumoSuave,
    surfaceContainerLowest = Carbon,
    surfaceContainerLow = Carbon1,
    surfaceContainer = Carbon2,
    surfaceContainerHigh = Carbon3,
    surfaceContainerHighest = Carbon4,
    outline = Color(0xFF3F3F46),
    outlineVariant = Carbon4,
    error = Color(0xFFFF8A80),
    onError = Color(0xFF3B0A0A),
    errorContainer = Color(0xFF3B0A0A),
    onErrorContainer = Color(0xFFFCA5A5),
    inverseSurface = Humo,
    inverseOnSurface = Negro
)

// Colores propios de la marca que Material no trae: tarjeta "hero" y estados de cotización
@Immutable
data class TratoColores(
    val hero: Color,
    val enHero: Color,
    val enHeroSuave: Color,
    val botonHero: Color,
    val enBotonHero: Color,
    val bordeHero: Color,
    val barraNavegacion: Color,
    val estados: Map<EstadoCotizacion, ColorEstado>,
    val enCola: ColorEstado
)

private val ColoresClaro = TratoColores(
    hero = Negro,
    enHero = Blanco,
    enHeroSuave = Color(0xFFB4B4BB),
    botonHero = Color(0xFFF4F4F5),
    enBotonHero = Negro,
    bordeHero = Negro,
    barraNavegacion = Blanco,
    estados = mapOf(
        EstadoCotizacion.BORRADOR to EstadosClaro.borrador,
        EstadoCotizacion.ENVIADA to EstadosClaro.enviada,
        EstadoCotizacion.ACEPTADA to EstadosClaro.aceptada,
        EstadoCotizacion.RECHAZADA to EstadosClaro.rechazada,
        EstadoCotizacion.FACTURADA to EstadosClaro.facturada,
        EstadoCotizacion.VENCIDA to EstadosClaro.vencida,
        EstadoCotizacion.ANULADA to EstadosClaro.anulada,
        EstadoCotizacion.REEMPLAZADA to EstadosClaro.reemplazada
    ),
    enCola = ColorEstado(AmbarClaro, Color(0xFFB45309))
)

private val ColoresOscuro = TratoColores(
    hero = Carbon4,
    enHero = Humo,
    enHeroSuave = HumoSuave,
    botonHero = Humo,
    enBotonHero = Negro,
    bordeHero = Color.Transparent,
    barraNavegacion = Carbon3,
    estados = mapOf(
        EstadoCotizacion.BORRADOR to EstadosOscuro.borrador,
        EstadoCotizacion.ENVIADA to EstadosOscuro.enviada,
        EstadoCotizacion.ACEPTADA to EstadosOscuro.aceptada,
        EstadoCotizacion.RECHAZADA to EstadosOscuro.rechazada,
        EstadoCotizacion.FACTURADA to EstadosOscuro.facturada,
        EstadoCotizacion.VENCIDA to EstadosOscuro.vencida,
        EstadoCotizacion.ANULADA to EstadosOscuro.anulada,
        EstadoCotizacion.REEMPLAZADA to EstadosOscuro.reemplazada
    ),
    enCola = ColorEstado(Color(0xFF3A2205), AmbarNoche)
)

val LocalTratoColores = staticCompositionLocalOf { ColoresClaro }

// Acceso corto desde cualquier pantalla: TratoTheme.colores.hero
object TratoTheme {
    val colores: TratoColores
        @Composable get() = LocalTratoColores.current
}

// Se usa la paleta propia de la marca (sin colores dinámicos) para mantener la identidad visual
@Composable
fun TratoHechoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalTratoColores provides if (darkTheme) ColoresOscuro else ColoresClaro) {
        MaterialTheme(
            colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
            typography = Typography,
            shapes = Formas,
            content = content
        )
    }
}
