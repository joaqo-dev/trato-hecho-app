package cl.duoc.tratohecho.data.remote.dto

// Respuesta de https://mindicador.cl/api (solo los campos que usamos)
data class MindicadorResponse(
    val uf: IndicadorDto,
    val dolar: IndicadorDto
)

data class IndicadorDto(
    val codigo: String,
    val nombre: String,
    val fecha: String,
    val valor: Double
)
