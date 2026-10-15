package cl.duoc.tratohecho.domain.model

// KPIs del panel de inicio (todos los montos en CLP)
data class Indicadores(
    val montoCotizadoMes: Double = 0.0,
    val cotizacionesDelMes: Int = 0,
    val tasaAceptacion: Double = 0.0,   // 0.0 a 1.0
    val montoFacturado: Double = 0.0,
    // Métricas accionables para la tarjeta principal
    val montoGanadoMes: Double = 0.0,      // aceptadas o facturadas este mes
    val ganadasMes: Int = 0,
    val porResponder: Int = 0,             // enviadas (o en cola) esperando respuesta del cliente
    val montoPorResponder: Double = 0.0,
    val vencenPronto: Int = 0,             // enviadas que vencen en los próximos 3 días
    val borradores: Int = 0,               // armadas pero aún no enviadas
    val porEstado: Map<EstadoCotizacion, Int> = emptyMap(),
    val rankingVendedores: List<VendedorRanking> = emptyList()
)

data class VendedorRanking(
    val nombre: String,
    val montoGanado: Double,
    val cantidadGanadas: Int
)
