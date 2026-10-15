package cl.duoc.tratohecho.data.repository

import cl.duoc.tratohecho.data.local.dao.CotizacionDao
import cl.duoc.tratohecho.data.local.entity.CotizacionConItems
import cl.duoc.tratohecho.data.local.entity.CotizacionEntity
import cl.duoc.tratohecho.data.local.entity.ItemCotizacionEntity
import cl.duoc.tratohecho.data.remote.TratoHechoApi
import cl.duoc.tratohecho.data.remote.llamarApi
import cl.duoc.tratohecho.data.sync.GestorSincronizacion
import cl.duoc.tratohecho.domain.CalculadoraCotizacion
import cl.duoc.tratohecho.domain.model.EstadoCotizacion
import cl.duoc.tratohecho.nativo.MonitorRed
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

// Resultado de presionar "Enviar"
sealed class ResultadoEnvio {
    // La API la registró como enviada: se puede abrir el correo
    data class Enviada(val cotizacion: CotizacionEntity) : ResultadoEnvio()
    // Sin señal: queda en cola y se avisará con una notificación cuando esté lista
    data class EnCola(val motivo: String) : ResultadoEnvio()
}

// Las cotizaciones se arman y guardan en Room (funciona sin conexión).
// GestorSincronizacion se encarga de subirlas a la API cuando hay señal.
@Singleton
class CotizacionRepository @Inject constructor(
    private val api: TratoHechoApi,
    private val cotizacionDao: CotizacionDao,
    private val gestorSincronizacion: GestorSincronizacion,
    private val monitorRed: MonitorRed
) {
    fun observarCotizaciones(): Flow<List<CotizacionEntity>> = cotizacionDao.observarCotizaciones()

    fun observarCotizacion(id: String): Flow<CotizacionConItems?> = cotizacionDao.observarCotizacion(id)

    suspend fun obtenerCotizacion(id: String): CotizacionConItems? = cotizacionDao.obtenerCotizacion(id)

    // Número provisorio mientras la API no asigna el correlativo
    fun numeroTemporal(): String = "TMP-" + (1000..9999).random()

    suspend fun guardarBorrador(cotizacion: CotizacionEntity, items: List<ItemCotizacionEntity>) {
        val totales = CalculadoraCotizacion.calcular(
            items, cotizacion.descuentoGlobalTipo, cotizacion.descuentoGlobalValor, cotizacion.moneda
        )
        val itemsOrdenados = items.mapIndexed { i, item -> item.copy(cotizacionId = cotizacion.id, orden = i) }
        cotizacionDao.guardarConItems(
            cotizacion.copy(total = totales.total, pendienteSync = true),
            itemsOrdenados
        )
    }

    // Deja la cotización en la cola de envío. Se envía en la próxima sincronización.
    suspend fun marcarParaEnvio(id: String) {
        val actual = cotizacionDao.obtenerCotizacion(id) ?: return
        cotizacionDao.guardarCotizacion(actual.cotizacion.copy(envioPendiente = true, pendienteSync = true))
    }

    // Pone la cotización en la cola de envío y, si hay señal, la sincroniza de inmediato
    suspend fun enviar(id: String): ResultadoEnvio {
        marcarParaEnvio(id)
        if (!monitorRed.estaConectado()) {
            return ResultadoEnvio.EnCola("Sin conexión: la cotización quedó en cola. Te avisaremos cuando esté lista para enviar.")
        }
        val resultado = gestorSincronizacion.sincronizarAhora(notificarListas = false)
        val enviada = resultado.getOrNull()?.listasParaEnviar?.find { it.id == id }
        return if (enviada != null) {
            ResultadoEnvio.Enviada(enviada)
        } else {
            ResultadoEnvio.EnCola(resultado.exceptionOrNull()?.message ?: "La cotización quedó en cola de envío")
        }
    }

    // Copia una cotización enviada como nuevo borrador (versión + 1). La anterior queda reemplazada.
    suspend fun crearNuevaVersion(id: String): String? {
        val actual = cotizacionDao.obtenerCotizacion(id) ?: return null
        val nuevaId = UUID.randomUUID().toString()
        val nueva = actual.cotizacion.copy(
            id = nuevaId,
            version = actual.cotizacion.version + 1,
            cotizacionOrigenId = actual.cotizacion.id,
            estado = EstadoCotizacion.BORRADOR,
            fechaCreacion = System.currentTimeMillis(),
            fechaEnvio = null,
            fechaVencimiento = null,
            fechaRespuesta = null,
            pendienteSync = true,
            envioPendiente = false
        )
        val items = actual.items.map { it.copy(id = UUID.randomUUID().toString(), cotizacionId = nuevaId) }
        cotizacionDao.guardarCotizacion(
            actual.cotizacion.copy(estado = EstadoCotizacion.REEMPLAZADA, pendienteSync = true)
        )
        cotizacionDao.guardarConItems(nueva, items)
        return nuevaId
    }

    // Devuelve true si la cotización se eliminó (era un borrador que nunca llegó a la API)
    suspend fun anular(id: String): Boolean {
        val actual = cotizacionDao.obtenerCotizacion(id)?.cotizacion ?: return false
        val nuncaSincronizada = actual.numero.startsWith("TMP-") && actual.estado == EstadoCotizacion.BORRADOR
        return if (nuncaSincronizada && actual.version == 1) {
            // Solo existe en el teléfono: se elimina directamente
            cotizacionDao.eliminar(id)
            true
        } else {
            cotizacionDao.guardarCotizacion(
                actual.copy(estado = EstadoCotizacion.ANULADA, envioPendiente = false, pendienteSync = true)
            )
            false
        }
    }

    // Demo: pide a la API simulada que el cliente responda la cotización
    suspend fun simularRespuesta(id: String): Result<CotizacionConItems> =
        llamarApi { api.simularRespuesta(id) }.map { respuesta ->
            cotizacionDao.guardarConItems(respuesta.cotizacion, respuesta.items)
            respuesta
        }
}
