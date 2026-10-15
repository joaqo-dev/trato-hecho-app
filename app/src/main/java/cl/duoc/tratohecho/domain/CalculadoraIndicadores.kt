package cl.duoc.tratohecho.domain

import cl.duoc.tratohecho.data.local.entity.CotizacionEntity
import cl.duoc.tratohecho.domain.model.EstadoCotizacion
import cl.duoc.tratohecho.domain.model.Indicadores
import cl.duoc.tratohecho.domain.model.VendedorRanking
import java.util.Calendar

// Calcula los indicadores del negocio a partir de las cotizaciones (montos convertidos a CLP)
object CalculadoraIndicadores {

    private val GANADAS = setOf(EstadoCotizacion.ACEPTADA, EstadoCotizacion.FACTURADA)
    private val PERDIDAS = setOf(EstadoCotizacion.RECHAZADA, EstadoCotizacion.VENCIDA)
    private val NO_CUENTAN = setOf(EstadoCotizacion.BORRADOR, EstadoCotizacion.ANULADA, EstadoCotizacion.REEMPLAZADA)

    fun calcular(cotizaciones: List<CotizacionEntity>, ahora: Long = System.currentTimeMillis()): Indicadores {
        // Las versiones reemplazadas no se cuentan para no duplicar montos
        val vigentes = cotizaciones.filter { it.estado != EstadoCotizacion.REEMPLAZADA }
        val delMes = vigentes.filter { it.estado !in NO_CUENTAN && esDelMes(it.fechaEnvio, ahora) }

        val ganadas = vigentes.count { it.estado in GANADAS }
        val perdidas = vigentes.count { it.estado in PERDIDAS }
        val tasa = if (ganadas + perdidas > 0) ganadas.toDouble() / (ganadas + perdidas) else 0.0

        val ranking = vigentes
            .filter { it.estado in GANADAS }
            .groupBy { it.vendedorNombre }
            .map { (nombre, lista) -> VendedorRanking(nombre, lista.sumOf { it.totalClp }, lista.size) }
            .sortedByDescending { it.montoGanado }

        return Indicadores(
            montoCotizadoMes = delMes.sumOf { it.totalClp },
            cotizacionesDelMes = delMes.size,
            tasaAceptacion = tasa,
            montoFacturado = vigentes.filter { it.estado == EstadoCotizacion.FACTURADA }.sumOf { it.totalClp },
            porEstado = vigentes.groupingBy { it.estado }.eachCount(),
            rankingVendedores = ranking
        )
    }

    private fun esDelMes(fecha: Long?, ahora: Long): Boolean {
        if (fecha == null) return false
        val hoy = Calendar.getInstance().apply { timeInMillis = ahora }
        val otra = Calendar.getInstance().apply { timeInMillis = fecha }
        return hoy.get(Calendar.YEAR) == otra.get(Calendar.YEAR) && hoy.get(Calendar.MONTH) == otra.get(Calendar.MONTH)
    }
}
