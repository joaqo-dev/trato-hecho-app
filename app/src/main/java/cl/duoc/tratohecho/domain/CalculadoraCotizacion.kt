package cl.duoc.tratohecho.domain

import cl.duoc.tratohecho.data.local.entity.ItemCotizacionEntity
import cl.duoc.tratohecho.domain.model.Moneda
import cl.duoc.tratohecho.domain.model.TipoDescuento
import cl.duoc.tratohecho.domain.model.TotalesCotizacion
import kotlin.math.pow
import kotlin.math.roundToLong

// Reglas de negocio para calcular los montos de una cotización.
// 1) Cada ítem: cantidad x precio - descuento del ítem
// 2) Descuento global sobre el subtotal
// 3) Impuesto de cada ítem sobre su monto ya descontado (proporcional al descuento global)
object CalculadoraCotizacion {

    fun calcularDescuento(base: Double, tipo: TipoDescuento, valor: Double): Double {
        val descuento = when (tipo) {
            TipoDescuento.PORCENTAJE -> base * valor / 100.0
            TipoDescuento.MONTO -> valor
        }
        return descuento.coerceIn(0.0, maxOf(base, 0.0))
    }

    fun subtotalItem(item: ItemCotizacionEntity): Double {
        val bruto = item.cantidad * item.precioUnitario
        return bruto - calcularDescuento(bruto, item.descuentoTipo, item.descuentoValor)
    }

    fun calcular(
        items: List<ItemCotizacionEntity>,
        tipoDescuentoGlobal: TipoDescuento,
        valorDescuentoGlobal: Double,
        moneda: Moneda
    ): TotalesCotizacion {
        val subtotal = items.sumOf { subtotalItem(it) }
        val descuentoGlobal = calcularDescuento(subtotal, tipoDescuentoGlobal, valorDescuentoGlobal)
        val neto = subtotal - descuentoGlobal
        val factor = if (subtotal > 0) neto / subtotal else 0.0
        val impuestos = items.sumOf { subtotalItem(it) * factor * it.impuestoPorcentaje / 100.0 }
        return TotalesCotizacion(
            subtotal = redondear(subtotal, moneda),
            descuentoGlobal = redondear(descuentoGlobal, moneda),
            neto = redondear(neto, moneda),
            impuestos = redondear(impuestos, moneda),
            total = redondear(neto + impuestos, moneda)
        )
    }

    fun redondear(valor: Double, moneda: Moneda): Double {
        val factor = 10.0.pow(moneda.decimales)
        return (valor * factor).roundToLong() / factor
    }

    // Convierte un precio del catálogo (en CLP) a la moneda de la cotización
    fun convertirDesdeClp(precioClp: Double, moneda: Moneda, tipoCambio: Double): Double =
        if (moneda == Moneda.CLP || tipoCambio <= 0) precioClp
        else redondear(precioClp / tipoCambio, moneda)
}
