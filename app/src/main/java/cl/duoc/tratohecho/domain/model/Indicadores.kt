package cl.duoc.tratohecho.domain.model

// KPIs del panel de inicio (todos los montos en CLP)
data class Indicadores(
    val montoCotizadoMes: Double = 0.0,
    val cotizacionesDelMes: Int = 0,
    val tasaAceptacion: Double = 0.0,   // 0.0 a 1.0
    val montoFacturado: Double = 0.0,
    val porEstado: Map<EstadoCotizacion, Int> = emptyMap(),
    val rankingVendedores: List<VendedorRanking> = emptyList()
)

data class VendedorRanking(
    val nombre: String,
    val montoGanado: Double,
    val cantidadGanadas: Int
)
