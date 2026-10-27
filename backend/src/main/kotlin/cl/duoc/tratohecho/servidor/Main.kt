package cl.duoc.tratohecho.servidor

import cl.duoc.tratohecho.servidor.datos.AlmacenJdbc
import cl.duoc.tratohecho.servidor.modelo.ErrorResponse
import cl.duoc.tratohecho.servidor.panel.panelRutas
import com.google.gson.Gson
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.request.httpMethod
import io.ktor.server.request.receiveText
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import java.io.File
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val gson = Gson()

// Arranca el servidor de Trato Hecho:
//   /v1/...   API REST que usa la app (la misma que antes respondía FakeApiInterceptor)
//   /panel    panel web para simular las respuestas de los clientes y la facturación
//   /salud    estado del servidor y de la base de datos
// Variables de entorno (ver .env.example):
//   PUERTO (8080), PANEL_CORREO, PANEL_CLAVE (obligatoria)
//   DB_URL, DB_USUARIO, DB_CLAVE → PostgreSQL (producción). Sin DB_URL se usa SQLite en BASE_DATOS (tratohecho.db).
fun main() {
    val puerto = System.getenv("PUERTO")?.toIntOrNull() ?: 8080
    val urlBase = System.getenv("DB_URL")
    val almacen = if (urlBase != null) {
        println("Base de datos: $urlBase")
        AlmacenJdbc(urlBase, System.getenv("DB_USUARIO"), System.getenv("DB_CLAVE"))
    } else {
        val archivo = File(System.getenv("BASE_DATOS") ?: "tratohecho.db")
        println("Base de datos: ${archivo.absolutePath} (SQLite)")
        AlmacenJdbc("jdbc:sqlite:${archivo.absolutePath}")
    }
    val servidor = ServidorTratoHecho(almacen)
    println("Trato Hecho escuchando en http://localhost:$puerto  (panel: http://localhost:$puerto/panel)")
    embeddedServer(Netty, port = puerto, host = "0.0.0.0") { modulo(servidor) }.start(wait = true)
}

fun Application.modulo(servidor: ServidorTratoHecho) {
    routing {
        get("/") { call.respondRedirect("/panel") }

        get("/salud") {
            val baseOk = servidor.baseDisponible()
            call.responderJson(
                if (baseOk) 200 else 503,
                gson.toJson(mapOf("estado" to if (baseOk) "ok" else "error", "baseDatos" to if (baseOk) "ok" else "sin conexión"))
            )
        }

        // Todas las rutas de la API pasan por ServidorTratoHecho.atender, que aplica las reglas de negocio
        route("/v1/{ruta...}") {
            handle {
                val metodo = call.request.httpMethod.value
                val ruta = call.parameters.getAll("ruta").orEmpty().filter { it.isNotBlank() }
                val cuerpo = call.receiveText().takeIf { it.isNotBlank() }
                val token = call.request.headers["Authorization"]
                val (codigo, json) = try {
                    val resultado = servidor.atender(metodo, ruta, cuerpo, token)
                    200 to (resultado?.let { gson.toJson(it) } ?: "{}")
                } catch (e: ErrorServidor) {
                    e.codigo to gson.toJson(ErrorResponse(e.message ?: "Error"))
                } catch (e: Exception) {
                    e.printStackTrace()
                    500 to gson.toJson(ErrorResponse("Error interno del servidor"))
                }
                registrar(metodo, ruta.joinToString("/"), codigo, servidor.usuarioDeToken(token))
                call.responderJson(codigo, json)
            }
        }

        panelRutas(servidor)
    }
}

suspend fun ApplicationCall.responderJson(codigo: Int, json: String) =
    respondText(json, ContentType.Application.Json, HttpStatusCode.fromValue(codigo))

// Muestra en la terminal (o en docker compose logs) los cambios y los errores.
// Las consultas GET exitosas no se muestran: la app sincroniza cada 15 segundos y llenarían el registro.
// Con REGISTRAR_TODO=1 se muestran todas.
private val registrarTodo = System.getenv("REGISTRAR_TODO") == "1"

private fun registrar(metodo: String, ruta: String, codigo: Int, usuario: String?) {
    if (metodo == "GET" && codigo < 400 && !registrarTodo) return
    val hora = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
    println("$hora  $codigo  ${metodo.padEnd(6)} /v1/$ruta  (${usuario ?: "-"})")
}
