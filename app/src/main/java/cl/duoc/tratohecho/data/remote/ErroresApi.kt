package cl.duoc.tratohecho.data.remote

import cl.duoc.tratohecho.data.remote.dto.ErrorResponse
import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

// Convierte una excepción de red/API en un mensaje entendible para el usuario
fun mensajeDeError(error: Throwable): String = when (error) {
    is IOException -> "Sin conexión. Revisa tu señal e inténtalo de nuevo."
    is HttpException -> {
        val cuerpo = error.response()?.errorBody()?.string()
        val mensaje = try {
            Gson().fromJson(cuerpo, ErrorResponse::class.java)?.mensaje
        } catch (e: Exception) {
            null
        }
        mensaje ?: "Error del servidor (${error.code()})"
    }
    else -> error.message ?: "Ocurrió un error inesperado"
}

// Ejecuta una llamada a la API y devuelve un Result con un mensaje de error amigable
suspend fun <T> llamarApi(bloque: suspend () -> T): Result<T> =
    try {
        Result.success(bloque())
    } catch (e: Exception) {
        Result.failure(Exception(mensajeDeError(e), e))
    }
