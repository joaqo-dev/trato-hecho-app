package cl.duoc.tratohecho.domain

import cl.duoc.tratohecho.data.local.entity.CotizacionEntity
import cl.duoc.tratohecho.domain.model.EstadoCotizacion

enum class EstadoPaso { COMPLETADO, EN_CURSO, PENDIENTE, FALLIDO }

data class PasoTracker(
    val titulo: String,
    val fecha: Long?,
    val estado: EstadoPaso
)

data class Progreso(
    val pasos: List<PasoTracker>,
    val avance: Float,          // 0.0 a 1.0: cuánto de la línea se pinta
    val inactiva: Boolean       // anulada o reemplazada: el tracker se muestra atenuado
)

// Convierte el estado de una cotización en los pasos del tracker:
// Creada → Enviada → Respuesta del cliente → Facturada
object ProgresoCotizacion {

    fun calcular(c: CotizacionEntity): Progreso {
        val enviada = c.fechaEnvio != null && c.estado != EstadoCotizacion.BORRADOR

        val creada = PasoTracker("Creada", c.fechaCreacion, EstadoPaso.COMPLETADO)

        val envio = when {
            c.envioPendiente -> PasoTracker("En cola", null, EstadoPaso.EN_CURSO)
            enviada -> PasoTracker("Enviada", c.fechaEnvio, EstadoPaso.COMPLETADO)
            else -> PasoTracker("Enviada", null, EstadoPaso.PENDIENTE)
        }

        val respuesta = when (c.estado) {
            EstadoCotizacion.ACEPTADA, EstadoCotizacion.FACTURADA ->
                PasoTracker("Aceptada", c.fechaRespuesta, EstadoPaso.COMPLETADO)
            EstadoCotizacion.RECHAZADA -> PasoTracker("Rechazada", c.fechaRespuesta, EstadoPaso.FALLIDO)
            EstadoCotizacion.VENCIDA -> PasoTracker("Vencida", c.fechaVencimiento, EstadoPaso.FALLIDO)
            else -> PasoTracker("Respuesta", null, EstadoPaso.PENDIENTE)
        }

        val facturada = if (c.estado == EstadoCotizacion.FACTURADA) {
            PasoTracker("Facturada", null, EstadoPaso.COMPLETADO)
        } else {
            PasoTracker("Facturada", null, EstadoPaso.PENDIENTE)
        }

        val pasos = listOf(creada, envio, respuesta, facturada)

        // La línea llega hasta el último paso cumplido y, si la cotización sigue viva,
        // avanza un poco hacia el siguiente (como un envío "en camino")
        val ultimo = pasos.indexOfLast { it.estado == EstadoPaso.COMPLETADO || it.estado == EstadoPaso.FALLIDO }
        val segmento = 1f / (pasos.size - 1)
        val sigueEnCurso = c.estado == EstadoCotizacion.BORRADOR || c.estado == EstadoCotizacion.ENVIADA ||
            c.estado == EstadoCotizacion.ACEPTADA
        val avance = (ultimo * segmento + if (sigueEnCurso) segmento * 0.35f else 0f).coerceIn(0f, 1f)

        val inactiva = c.estado == EstadoCotizacion.ANULADA || c.estado == EstadoCotizacion.REEMPLAZADA
        return Progreso(pasos, avance, inactiva)
    }
}
