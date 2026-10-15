package cl.duoc.tratohecho.domain.model

data class TotalesCotizacion(
    val subtotal: Double = 0.0,        // suma de ítems con su descuento propio
    val descuentoGlobal: Double = 0.0,
    val neto: Double = 0.0,            // subtotal - descuento global
    val impuestos: Double = 0.0,
    val total: Double = 0.0
)
