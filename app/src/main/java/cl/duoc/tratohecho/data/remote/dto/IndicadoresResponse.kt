package cl.duoc.tratohecho.data.remote.dto

// Respuesta de https://findic.cl/api/ (solo los campos que usamos)
data class IndicadoresResponse(
    val uf: IndicadorDto,
    val dolar: IndicadorDto
)

data class IndicadorDto(
    val codigo: String,
    val nombre: String,
    val fecha: String,   // "2026-10-15"
    val valor: Double
)
