package cl.duoc.tratohecho.data.remote

import cl.duoc.tratohecho.data.local.SesionDataStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

// Agrega el token de la sesión a cada petición. Con él la API sabe a qué empresa pertenece el usuario.
class AuthInterceptor(
    private val sesionDataStore: SesionDataStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { sesionDataStore.obtenerSesion()?.token }
        val request = if (token != null) {
            chain.request().newBuilder().header("Authorization", "Bearer $token").build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}
