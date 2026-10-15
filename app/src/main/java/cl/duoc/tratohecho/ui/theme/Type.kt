package cl.duoc.tratohecho.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Fuente del sistema con escalones de peso claros. Los montos usan cifras tabulares ("tnum")
// para que no "bailen" al animarse o al compararse en columnas.
private val Sistema = FontFamily.Default

val Typography = Typography(
    displaySmall = TextStyle(
        fontFamily = Sistema, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 40.sp,
        letterSpacing = (-0.8).sp, fontFeatureSettings = "tnum"
    ),
    headlineSmall = TextStyle(
        fontFamily = Sistema, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp,
        letterSpacing = (-0.4).sp
    ),
    titleLarge = TextStyle(
        fontFamily = Sistema, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp,
        letterSpacing = (-0.2).sp
    ),
    titleMedium = TextStyle(
        fontFamily = Sistema, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp,
        fontFeatureSettings = "tnum"
    ),
    titleSmall = TextStyle(
        fontFamily = Sistema, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(fontFamily = Sistema, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = Sistema, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = Sistema, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = Sistema, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = Sistema, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = Sistema, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp)
)
