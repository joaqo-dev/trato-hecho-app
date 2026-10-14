package cl.duoc.tratohecho.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaClaro = lightColorScheme(
    primary = AzulPetroleo,
    onPrimary = Color.White,
    primaryContainer = AzulPetroleoClaro,
    onPrimaryContainer = AzulPetroleoOscuro,
    secondary = Ambar,
    onSecondary = Color.White,
    secondaryContainer = AmbarClaro,
    onSecondaryContainer = AmbarOscuro,
    tertiary = VerdeAcuerdo,
    onTertiary = Color.White,
    tertiaryContainer = VerdeAcuerdoClaro,
    background = FondoClaro,
    surface = SuperficieClara,
    error = ErrorRojo
)

private val EsquemaOscuro = darkColorScheme(
    primary = Color(0xFF8FCDE6),
    onPrimary = AzulPetroleoOscuro,
    primaryContainer = Color(0xFF1F4B5E),
    onPrimaryContainer = AzulPetroleoClaro,
    secondary = Color(0xFFFFB870),
    onSecondary = AmbarOscuro,
    secondaryContainer = Color(0xFF7A4A10),
    onSecondaryContainer = AmbarClaro,
    tertiary = Color(0xFF8FD6B3),
    background = FondoOscuro,
    surface = SuperficieOscura,
    error = Color(0xFFFFB4AB)
)

// Se usa la paleta propia de la marca (sin colores dinámicos) para mantener la identidad visual
@Composable
fun TratoHechoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography = Typography,
        shapes = Formas,
        content = content
    )
}
