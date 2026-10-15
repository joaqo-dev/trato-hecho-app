package cl.duoc.tratohecho.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta "Trato Hecho": monocromo de alto contraste + ámbar de la marca como único acento.
// Pensada para leerse a pleno sol en terreno.

// ---------- Neutros (tema claro) ----------
val Negro = Color(0xFF0A0A0A)
val Blanco = Color(0xFFFFFFFF)
val Gris50 = Color(0xFFF7F7F8)
val Gris100 = Color(0xFFF4F4F5)
val Gris150 = Color(0xFFEDEDF0)
val Gris200 = Color(0xFFE6E6EA)
val Gris300 = Color(0xFFD4D4D8)
val Gris500 = Color(0xFF6B6B72)
val Gris700 = Color(0xFF3F3F46)

// ---------- Neutros (tema oscuro) ----------
val Carbon = Color(0xFF0B0B0C)
val Carbon1 = Color(0xFF141416)
val Carbon2 = Color(0xFF1C1C1F)
val Carbon3 = Color(0xFF242428)
val Carbon4 = Color(0xFF2C2C31)
val Humo = Color(0xFFF5F5F6)
val HumoSuave = Color(0xFFA1A1AA)

// ---------- Acento de la marca ----------
val Ambar = Color(0xFFE8963A)
val AmbarClaro = Color(0xFFFFF1DE)
val AmbarOscuro = Color(0xFF5C3300)
val AmbarNoche = Color(0xFFFFB870)

// ---------- Estados de cotización (fondo suave + texto) ----------
data class ColorEstado(val fondo: Color, val texto: Color)

object EstadosClaro {
    val borrador = ColorEstado(Color(0xFFEFEFF1), Color(0xFF52525B))
    val enviada = ColorEstado(Color(0xFFE8F0FE), Color(0xFF2563EB))
    val aceptada = ColorEstado(Color(0xFFE3F6EA), Color(0xFF15803D))
    val rechazada = ColorEstado(Color(0xFFFDECEC), Color(0xFFC62828))
    val facturada = ColorEstado(Color(0xFFF1EBFD), Color(0xFF6D28D9))
    val vencida = ColorEstado(Color(0xFFFFF1DE), Color(0xFFB45309))
    val anulada = ColorEstado(Color(0xFFE4E4E7), Color(0xFF3F3F46))
    val reemplazada = ColorEstado(Color(0xFFF4F4F5), Color(0xFF71717A))
}

object EstadosOscuro {
    val borrador = ColorEstado(Color(0xFF27272A), Color(0xFFD4D4D8))
    val enviada = ColorEstado(Color(0xFF172554), Color(0xFF93C5FD))
    val aceptada = ColorEstado(Color(0xFF052E16), Color(0xFF86EFAC))
    val rechazada = ColorEstado(Color(0xFF3B0A0A), Color(0xFFFCA5A5))
    val facturada = ColorEstado(Color(0xFF2E1065), Color(0xFFC4B5FD))
    val vencida = ColorEstado(Color(0xFF3A2205), AmbarNoche)
    val anulada = ColorEstado(Color(0xFF27272A), Color(0xFFA1A1AA))
    val reemplazada = ColorEstado(Color(0xFF1C1C1F), Color(0xFF8B8B93))
}
