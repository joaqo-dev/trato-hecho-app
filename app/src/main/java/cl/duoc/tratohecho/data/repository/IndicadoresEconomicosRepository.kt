package cl.duoc.tratohecho.data.repository

import cl.duoc.tratohecho.data.local.IndicadoresDataStore
import cl.duoc.tratohecho.data.remote.MindicadorApi
import cl.duoc.tratohecho.domain.model.IndicadorEconomico
import cl.duoc.tratohecho.domain.model.Moneda
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

// Valor del dólar y la UF desde mindicador.cl. Si no hay conexión se usa el último valor guardado.
@Singleton
class IndicadoresEconomicosRepository @Inject constructor(
    private val api: MindicadorApi,
    private val cache: IndicadoresDataStore
) {
    suspend fun obtener(moneda: Moneda): Result<IndicadorEconomico> {
        if (moneda == Moneda.CLP) {
            return Result.success(IndicadorEconomico(Moneda.CLP, 1.0, "", desdeCache = false))
        }
        return try {
            val respuesta = api.obtenerIndicadores()
            val dto = if (moneda == Moneda.UF) respuesta.uf else respuesta.dolar
            val indicador = IndicadorEconomico(moneda, dto.valor, formatearFecha(dto.fecha), desdeCache = false)
            cache.guardar(indicador)
            Result.success(indicador)
        } catch (e: Exception) {
            val guardado = cache.obtener(moneda)
            if (guardado != null) Result.success(guardado)
            else Result.failure(Exception("No se pudo obtener el valor de ${moneda.etiqueta} y no hay un valor guardado"))
        }
    }

    // mindicador entrega la fecha en formato ISO (2026-10-14T03:00:00.000Z)
    private fun formatearFecha(fechaIso: String): String = try {
        val entrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val fecha = entrada.parse(fechaIso.take(19))
        SimpleDateFormat("dd-MM-yyyy", Locale.forLanguageTag("es-CL")).format(fecha!!)
    } catch (e: Exception) {
        fechaIso.take(10)
    }
}
