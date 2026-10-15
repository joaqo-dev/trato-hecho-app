package cl.duoc.tratohecho.domain.model

// Valor del dólar o la UF obtenido desde mindicador.cl (o desde el último valor guardado)
data class IndicadorEconomico(
    val moneda: Moneda,
    val valor: Double,
    val fecha: String,
    val desdeCache: Boolean
)
