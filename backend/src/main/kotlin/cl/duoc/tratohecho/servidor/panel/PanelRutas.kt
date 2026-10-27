package cl.duoc.tratohecho.servidor.panel

import cl.duoc.tratohecho.servidor.ErrorServidor
import cl.duoc.tratohecho.servidor.ServidorTratoHecho
import cl.duoc.tratohecho.servidor.modelo.ErrorResponse
import cl.duoc.tratohecho.servidor.modelo.LoginRequest
import cl.duoc.tratohecho.servidor.responderJson
import com.google.gson.Gson
import io.ktor.http.ContentType
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveText
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

private val gson = Gson()

// Sesiones abiertas del panel (se pierden al reiniciar el servidor y hay que volver a ingresar)
private val tokensPanel = ConcurrentHashMap.newKeySet<String>()

private val correoPanel = System.getenv("PANEL_CORREO") ?: "panel@tratohecho.cl"
private val clavePanel = System.getenv("PANEL_CLAVE")?.takeIf { it.isNotBlank() }
    ?: error("Falta la variable de entorno PANEL_CLAVE (clave del panel web)")

// Panel web: simula lo que pasa fuera de la app (el cliente responde en la página pública
// y la empresa factura). Ve todas las empresas, por eso tiene su propio usuario.
fun Route.panelRutas(servidor: ServidorTratoHecho) {
    get("/panel") {
        val html = ServidorTratoHecho::class.java.getResource("/panel/index.html")!!.readText()
        call.respondText(html, ContentType.Text.Html)
    }

    route("/panel/api") {
        post("/login") {
            val request = gson.fromJson(call.receiveText(), LoginRequest::class.java)
            if (request?.correo?.trim().equals(correoPanel, ignoreCase = true) && request.clave == clavePanel) {
                val token = UUID.randomUUID().toString()
                tokensPanel += token
                call.responderJson(200, gson.toJson(mapOf("token" to token)))
            } else {
                call.responderJson(401, gson.toJson(ErrorResponse("Correo o clave incorrectos")))
            }
        }
        get("/empresas") { protegido { servidor.panelEmpresas() } }
        get("/empresas/{id}") { protegido { servidor.panelEmpresa(call.parameters["id"]!!) } }
        post("/cotizaciones/{id}/aceptar") { protegido { servidor.responderCotizacion(call.parameters["id"]!!, aceptada = true) } }
        post("/cotizaciones/{id}/rechazar") { protegido { servidor.responderCotizacion(call.parameters["id"]!!, aceptada = false) } }
        post("/cotizaciones/{id}/facturar") { protegido { servidor.facturarCotizacion(call.parameters["id"]!!) } }
        post("/reiniciar") { protegido { servidor.reiniciarDatos(); mapOf("ok" to true) } }
    }
}

private suspend fun io.ktor.server.routing.RoutingContext.protegido(accion: () -> Any) {
    val token = call.request.headers["Authorization"]?.removePrefix("Bearer ")
    if (token == null || token !in tokensPanel) {
        call.responderJson(401, gson.toJson(ErrorResponse("Sesión del panel no válida")))
        return
    }
    call.responderResultado(accion)
}

private suspend fun ApplicationCall.responderResultado(accion: () -> Any) {
    try {
        responderJson(200, gson.toJson(accion()))
    } catch (e: ErrorServidor) {
        responderJson(e.codigo, gson.toJson(ErrorResponse(e.message ?: "Error")))
    }
}
