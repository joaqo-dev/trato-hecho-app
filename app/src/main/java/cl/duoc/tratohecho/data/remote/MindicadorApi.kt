package cl.duoc.tratohecho.data.remote

import cl.duoc.tratohecho.data.remote.dto.MindicadorResponse
import retrofit2.http.GET

// Fuente pública de indicadores económicos de Chile (esta sí es una API real)
interface MindicadorApi {
    @GET("api")
    suspend fun obtenerIndicadores(): MindicadorResponse
}
