package cl.duoc.tratohecho.data.repository

import cl.duoc.tratohecho.data.local.IndicadoresDataStore
import cl.duoc.tratohecho.data.remote.IndicadoresApi
import cl.duoc.tratohecho.domain.model.IndicadorEconomico
import cl.duoc.tratohecho.domain.model.Moneda
import cl.duoc.tratohecho.nativo.MonitorRed
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

// Valor del dólar y la UF desde findic.cl.
// Cada consulta exitosa queda guardada: sin conexión se usa el último valor obtenido (desdeCache = true).
@Singleton
class IndicadoresEconomicosRepository @Inject constructor(
    private val api: IndicadoresApi,
    private val cache: IndicadoresDataStore,
    private val monitorRed: MonitorRed
) {
    suspend fun obtener(moneda: Moneda): Result<IndicadorEconomico> {
        if (moneda == Moneda.CLP) {
            return Result.success(IndicadorEconomico(Moneda.CLP, 1.0, "", desdeCache = false))
        }
        if (monitorRed.estaConectado() && actualizar()) {
            cache.obtener(moneda)?.let { return Result.success(it.copy(desdeCache = false)) }
        }
        val guardado = cache.obtener(moneda)
        return if (guardado != null) {
            Result.success(guardado)
        } else {
            Result.failure(
                Exception("No hay conexión y la app aún no tiene un valor de ${moneda.etiqueta} guardado. Conéctate una vez para descargarlo.")
            )
        }
    }

    // Descarga ambos valores y los guarda. Se llama al sincronizar para tener siempre un respaldo.
    suspend fun actualizar(): Boolean = try {
        val respuesta = api.obtenerIndicadores()
        cache.guardar(IndicadorEconomico(Moneda.UF, respuesta.uf.valor, formatearFecha(respuesta.uf.fecha), desdeCache = true))
        cache.guardar(IndicadorEconomico(Moneda.USD, respuesta.dolar.valor, formatearFecha(respuesta.dolar.fecha), desdeCache = true))
        true
    } catch (e: Exception) {
        false
    }

    // findic entrega la fecha como "2026-10-15"; se muestra como "15-10-2026"
    private fun formatearFecha(fecha: String): String = try {
        val entrada = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        SimpleDateFormat("dd-MM-yyyy", Locale.forLanguageTag("es-CL")).format(entrada.parse(fecha.take(10))!!)
    } catch (e: Exception) {
        fecha
    }
}
