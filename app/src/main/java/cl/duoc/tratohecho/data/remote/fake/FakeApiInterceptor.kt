package cl.duoc.tratohecho.data.remote.fake

import cl.duoc.tratohecho.data.remote.dto.ErrorResponse
import cl.duoc.tratohecho.nativo.MonitorRed
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.io.IOException

// Interceptor de OkHttp que responde las peticiones a la API de Trato Hecho sin salir a internet.
// Así Retrofit funciona igual que con un servidor real: con demora, códigos HTTP y JSON.
// Si el teléfono no tiene conexión, falla igual que lo haría una API real.
class FakeApiInterceptor(
    private val servidor: ServidorSimulado,
    private val monitorRed: MonitorRed
) : Interceptor {

    private val gson = Gson()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (!monitorRed.estaConectado()) {
            throw IOException("Sin conexión a internet")
        }
        Thread.sleep(DEMORA_MS)

        val ruta = request.url.pathSegments.filter { it.isNotBlank() }.drop(1) // se quita "v1"
        val cuerpo = request.body?.let { body ->
            val buffer = Buffer()
            body.writeTo(buffer)
            buffer.readUtf8()
        }
        val token = request.header("Authorization")

        val (codigo, json) = try {
            val resultado = servidor.atender(request.method, ruta, cuerpo, token)
            200 to (resultado?.let { gson.toJson(it) } ?: "{}")
        } catch (e: ErrorServidor) {
            e.codigo to gson.toJson(ErrorResponse(e.message ?: "Error"))
        }

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(codigo)
            .message(if (codigo < 300) "OK" else "Error")
            .body(json.toResponseBody("application/json".toMediaType()))
            .build()
    }

    companion object {
        private const val DEMORA_MS = 500L
    }
}
