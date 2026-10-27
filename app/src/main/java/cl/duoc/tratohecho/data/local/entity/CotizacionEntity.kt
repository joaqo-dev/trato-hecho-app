package cl.duoc.tratohecho.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import cl.duoc.tratohecho.domain.model.EstadoCotizacion
import cl.duoc.tratohecho.domain.model.Moneda
import cl.duoc.tratohecho.domain.model.TipoDescuento

@Entity(tableName = "cotizaciones")
data class CotizacionEntity(
    @PrimaryKey val id: String,
    val empresaId: String,
    val numero: String,                 // "TMP-1234" sin conexión, "COT-0012" cuando la API lo asigna
    val version: Int = 1,
    val cotizacionOrigenId: String? = null, // versión anterior (si es una nueva versión)
    val clienteId: String,
    val clienteNombre: String,
    val contactoId: String? = null,
    val vendedorId: String,
    val vendedorNombre: String,
    val moneda: Moneda = Moneda.CLP,
    val tipoCambio: Double = 1.0,       // valor en CLP de 1 USD o 1 UF
    val fechaTipoCambio: String? = null,
    val diasValidez: Int = 15,
    val descuentoGlobalTipo: TipoDescuento = TipoDescuento.PORCENTAJE,
    val descuentoGlobalValor: Double = 0.0,
    val notas: String = "",
    val estado: EstadoCotizacion = EstadoCotizacion.BORRADOR,
    val total: Double = 0.0,            // total en la moneda de la cotización
    val fechaCreacion: Long,
    val fechaEnvio: Long? = null,
    val fechaVencimiento: Long? = null,
    val fechaRespuesta: Long? = null,
    val fechaFacturacion: Long? = null,  // la empresa registró la factura (desde la plataforma web)
    val pendienteSync: Boolean = false,
    // true = el vendedor presionó "Enviar" sin conexión y quedó en cola
    val envioPendiente: Boolean = false
) {
    val numeroCompleto: String get() = if (version > 1) "$numero v$version" else numero

    // Total convertido a pesos (para indicadores)
    val totalClp: Double get() = if (moneda == Moneda.CLP) total else total * tipoCambio
}
