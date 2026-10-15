package cl.duoc.tratohecho.data.remote

import cl.duoc.tratohecho.data.remote.dto.IndicadoresResponse
import retrofit2.http.GET

// Fuente pública de indicadores económicos de Chile (findic.cl, sin clave).
// Esta sí es una API real: entrega el valor del día de la UF y del dólar observado.
interface IndicadoresApi {
    @GET("api/")
    suspend fun obtenerIndicadores(): IndicadoresResponse
}
