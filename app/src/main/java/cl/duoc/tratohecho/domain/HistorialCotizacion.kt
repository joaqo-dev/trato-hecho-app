package cl.duoc.tratohecho.domain

import cl.duoc.tratohecho.data.local.entity.CotizacionEntity
import cl.duoc.tratohecho.domain.model.EstadoCotizacion

enum class TipoEvento { HECHO, PENDIENTE, ALERTA, ERROR }

data class EventoHistorial(
    val titulo: String,
    val detalle: String,
    val fecha: Long?,
    val tipo: TipoEvento
)

// Arma la línea de tiempo de una cotización a partir de sus fechas y su estado
object HistorialCotizacion {

    // Agrega el punto final solo si el texto no termina ya en uno (ej: "Comercial Biobío Ltda.")
    private fun conPunto(texto: String): String = if (texto.endsWith(".")) texto else "$texto."

    fun eventos(c: CotizacionEntity): List<EventoHistorial> {
        val lista = mutableListOf<EventoHistorial>()

        lista += EventoHistorial(
            titulo = if (c.version > 1) "Versión ${c.version} creada" else "Borrador creado",
            detalle = if (c.version > 1) conPunto("Se creó a partir de la versión anterior para ${c.clienteNombre}")
            else conPunto("${c.vendedorNombre} armó la cotización para ${c.clienteNombre}"),
            fecha = c.fechaCreacion,
            tipo = TipoEvento.HECHO
        )

        if (c.envioPendiente) {
            lista += EventoHistorial(
                "En cola de envío",
                "Se enviará automáticamente al recuperar la señal.",
                null,
                TipoEvento.PENDIENTE
            )
        }

        c.fechaEnvio?.let {
            lista += EventoHistorial(
                "Enviada al cliente",
                "Se registró el envío con el PDF. Es válida por ${c.diasValidez} días.",
                it,
                TipoEvento.HECHO
            )
        }

        when (c.estado) {
            EstadoCotizacion.ACEPTADA, EstadoCotizacion.FACTURADA -> lista += EventoHistorial(
                "Aceptada por el cliente",
                "El cliente aceptó la propuesta en la página pública de Trato Hecho.",
                c.fechaRespuesta,
                TipoEvento.HECHO
            )
            EstadoCotizacion.RECHAZADA -> lista += EventoHistorial(
                "Rechazada por el cliente",
                "El cliente rechazó la propuesta. Puedes crear una nueva versión.",
                c.fechaRespuesta,
                TipoEvento.ERROR
            )
            EstadoCotizacion.VENCIDA -> lista += EventoHistorial(
                "Venció sin respuesta",
                "Pasaron los ${c.diasValidez} días de validez sin respuesta del cliente.",
                c.fechaVencimiento,
                TipoEvento.ALERTA
            )
            else -> Unit
        }

        if (c.estado == EstadoCotizacion.FACTURADA) {
            lista += EventoHistorial("Facturada", "Se registró la factura de esta cotización.", c.fechaFacturacion, TipoEvento.HECHO)
        }
        if (c.estado == EstadoCotizacion.ENVIADA) {
            lista += EventoHistorial(
                "Esperando respuesta",
                "Vence el día indicado si el cliente no responde.",
                c.fechaVencimiento,
                TipoEvento.PENDIENTE
            )
        }
        if (c.estado == EstadoCotizacion.ANULADA) {
            lista += EventoHistorial("Anulada", "La cotización se anuló y ya no puede aceptarse.", null, TipoEvento.ERROR)
        }
        if (c.estado == EstadoCotizacion.REEMPLAZADA) {
            lista += EventoHistorial("Reemplazada", "Existe una versión más nueva de esta cotización.", null, TipoEvento.ALERTA)
        }
        return lista
    }
}
