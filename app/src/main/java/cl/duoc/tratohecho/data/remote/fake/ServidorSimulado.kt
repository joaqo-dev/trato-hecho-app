package cl.duoc.tratohecho.data.remote.fake

import android.content.Context
import cl.duoc.tratohecho.data.local.entity.ClienteConContactos
import cl.duoc.tratohecho.data.local.entity.CotizacionConItems
import cl.duoc.tratohecho.data.local.entity.EmpresaEntity
import cl.duoc.tratohecho.data.local.entity.ImpuestoEntity
import cl.duoc.tratohecho.data.local.entity.ProductoEntity
import cl.duoc.tratohecho.data.local.entity.UnidadEntity
import cl.duoc.tratohecho.data.local.entity.UsuarioEntity
import cl.duoc.tratohecho.data.remote.dto.LoginRequest
import cl.duoc.tratohecho.data.remote.dto.LoginResponse
import cl.duoc.tratohecho.data.remote.dto.UsuarioRequest
import cl.duoc.tratohecho.domain.model.EstadoCotizacion
import cl.duoc.tratohecho.domain.model.Rol
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

// Error que el servidor devuelve como respuesta HTTP (ej: 401, 403, 404)
class ErrorServidor(val codigo: Int, mensaje: String) : Exception(mensaje)

// Simula el backend de Trato Hecho. Guarda sus datos en un archivo JSON para que
// sobrevivan al cerrar la app. Aplica las reglas del servidor:
// - Cada usuario solo ve los datos de su empresa (multi-tenant).
// - Un vendedor solo ve sus clientes y cotizaciones; supervisor y administrador ven todo.
// - Asigna el número correlativo de cada cotización.
// - Cambia el estado de las cotizaciones enviadas (respuesta del cliente y vencimiento).
@Singleton
class ServidorSimulado @Inject constructor(
    @ApplicationContext context: Context
) {
    private val gson = Gson()
    private val archivo = File(context.filesDir, "servidor_simulado.json")
    private val bd: BaseDatosServidor = cargar()

    @Synchronized
    fun atender(metodo: String, ruta: List<String>, cuerpo: String?, token: String?): Any? {
        if (ruta == listOf("auth", "login") && metodo == "POST") {
            return login(gson.fromJson(cuerpo, LoginRequest::class.java))
        }
        val usuario = usuarioDesdeToken(token)
        val resultado: Any? = when (ruta.firstOrNull()) {
            "empresa" -> empresa(metodo, cuerpo, usuario)
            "usuarios" -> usuarios(metodo, ruta.getOrNull(1), cuerpo, usuario)
            "unidades" -> catalogo(metodo, ruta.getOrNull(1), cuerpo, usuario, bd.unidades, UnidadEntity::class.java) { it.id }
            "impuestos" -> catalogo(metodo, ruta.getOrNull(1), cuerpo, usuario, bd.impuestos, ImpuestoEntity::class.java) { it.id }
            "productos" -> catalogo(metodo, ruta.getOrNull(1), cuerpo, usuario, bd.productos, ProductoEntity::class.java) { it.id }
            "clientes" -> clientes(metodo, ruta.getOrNull(1), cuerpo, usuario)
            "cotizaciones" -> cotizaciones(metodo, ruta.getOrNull(1), ruta.getOrNull(2), cuerpo, usuario)
            else -> throw ErrorServidor(404, "Ruta no encontrada")
        }
        if (metodo != "GET") guardar()
        return resultado
    }

    // ---------------- Autenticación ----------------

    private fun login(request: LoginRequest): LoginResponse {
        val encontrado = bd.usuarios.find {
            it.usuario.correo.equals(request.correo.trim(), ignoreCase = true) && it.clave == request.clave
        } ?: throw ErrorServidor(401, "Correo o clave incorrectos")
        if (!encontrado.usuario.activo) throw ErrorServidor(403, "Tu usuario está desactivado. Contacta al administrador.")
        val empresa = bd.empresas.first { it.id == encontrado.usuario.empresaId }
        return LoginResponse(token = "token-${encontrado.usuario.id}", usuario = encontrado.usuario, empresa = empresa)
    }

    private fun usuarioDesdeToken(token: String?): UsuarioEntity {
        val id = token?.removePrefix("Bearer ")?.removePrefix("token-")
        val usuario = bd.usuarios.find { it.usuario.id == id }?.usuario
            ?: throw ErrorServidor(401, "Sesión no válida")
        if (!usuario.activo) throw ErrorServidor(403, "Tu usuario está desactivado")
        return usuario
    }

    private fun exigirAdministrador(usuario: UsuarioEntity) {
        if (usuario.rol != Rol.ADMINISTRADOR) throw ErrorServidor(403, "Solo el administrador puede realizar esta acción")
    }

    // ---------------- Empresa ----------------

    private fun empresa(metodo: String, cuerpo: String?, usuario: UsuarioEntity): EmpresaEntity {
        val indice = bd.empresas.indexOfFirst { it.id == usuario.empresaId }
        if (metodo == "GET") return bd.empresas[indice]
        exigirAdministrador(usuario)
        val empresa = gson.fromJson(cuerpo, EmpresaEntity::class.java).copy(id = usuario.empresaId)
        bd.empresas[indice] = empresa
        return empresa
    }

    // ---------------- Usuarios ----------------

    private fun usuarios(metodo: String, id: String?, cuerpo: String?, usuario: UsuarioEntity): Any? {
        exigirAdministrador(usuario)
        when (metodo) {
            "GET" -> return bd.usuarios.filter { it.usuario.empresaId == usuario.empresaId }.map { it.usuario }
            "PUT" -> {
                val request = gson.fromJson(cuerpo, UsuarioRequest::class.java)
                val nuevo = request.usuario.copy(id = id!!, empresaId = usuario.empresaId)
                val correoRepetido = bd.usuarios.any {
                    it.usuario.id != id && it.usuario.correo.equals(nuevo.correo, ignoreCase = true)
                }
                if (correoRepetido) throw ErrorServidor(409, "Ya existe un usuario con ese correo")
                val indice = bd.usuarios.indexOfFirst { it.usuario.id == id }
                if (indice >= 0) {
                    if (bd.usuarios[indice].usuario.empresaId != usuario.empresaId) throw ErrorServidor(404, "Usuario no encontrado")
                    if (id == usuario.id && (!nuevo.activo || nuevo.rol != Rol.ADMINISTRADOR)) {
                        throw ErrorServidor(409, "No puedes desactivarte ni quitarte el rol de administrador")
                    }
                    val clave = request.clave?.takeIf { it.isNotBlank() } ?: bd.usuarios[indice].clave
                    bd.usuarios[indice] = UsuarioServidor(nuevo, clave)
                } else {
                    val clave = request.clave?.takeIf { it.isNotBlank() }
                        ?: throw ErrorServidor(400, "La clave es obligatoria para un usuario nuevo")
                    bd.usuarios += UsuarioServidor(nuevo, clave)
                }
                return nuevo
            }
            "DELETE" -> {
                if (id == usuario.id) throw ErrorServidor(409, "No puedes eliminar tu propio usuario")
                val tieneDocumentos = bd.cotizaciones.any { it.cotizacion.vendedorId == id } ||
                    bd.clientes.any { it.cliente.vendedorId == id }
                if (tieneDocumentos) throw ErrorServidor(409, "El usuario tiene clientes o cotizaciones. Desactívalo en vez de eliminarlo.")
                bd.usuarios.removeAll { it.usuario.id == id && it.usuario.empresaId == usuario.empresaId }
                return null
            }
        }
        throw ErrorServidor(405, "Método no permitido")
    }

    // ---------------- Catálogo (unidades, impuestos y productos) ----------------

    private fun <T> catalogo(
        metodo: String,
        id: String?,
        cuerpo: String?,
        usuario: UsuarioEntity,
        lista: MutableList<T>,
        clase: Class<T>,
        idDe: (T) -> String
    ): Any? {
        val empresaDe: (T) -> String = { elemento ->
            when (elemento) {
                is UnidadEntity -> elemento.empresaId
                is ImpuestoEntity -> elemento.empresaId
                is ProductoEntity -> elemento.empresaId
                else -> ""
            }
        }
        when (metodo) {
            "GET" -> return lista.filter { empresaDe(it) == usuario.empresaId }
            "PUT" -> {
                exigirAdministrador(usuario)
                val elemento = gson.fromJson(cuerpo, clase)
                if (empresaDe(elemento) != usuario.empresaId) throw ErrorServidor(403, "No pertenece a tu empresa")
                val indice = lista.indexOfFirst { idDe(it) == id }
                if (indice >= 0) lista[indice] = elemento else lista += elemento
                return elemento
            }
            "DELETE" -> {
                exigirAdministrador(usuario)
                val enUso = when (clase) {
                    UnidadEntity::class.java -> bd.productos.any { it.unidadId == id }
                    ImpuestoEntity::class.java -> bd.productos.any { it.impuestoId == id }
                    else -> false
                }
                if (enUso) throw ErrorServidor(409, "No se puede eliminar: hay productos que lo usan")
                lista.removeAll { idDe(it) == id && empresaDe(it) == usuario.empresaId }
                return null
            }
        }
        throw ErrorServidor(405, "Método no permitido")
    }

    // ---------------- Clientes ----------------

    private fun puedeVer(usuario: UsuarioEntity, empresaId: String, vendedorId: String): Boolean =
        empresaId == usuario.empresaId && (usuario.rol != Rol.VENDEDOR || vendedorId == usuario.id)

    private fun clientes(metodo: String, id: String?, cuerpo: String?, usuario: UsuarioEntity): Any? {
        when (metodo) {
            "GET" -> return bd.clientes.filter { puedeVer(usuario, it.cliente.empresaId, it.cliente.vendedorId) }
            "PUT" -> {
                if (usuario.rol == Rol.ADMINISTRADOR) throw ErrorServidor(403, "El administrador solo puede consultar clientes")
                val cliente = gson.fromJson(cuerpo, ClienteConContactos::class.java)
                if (!puedeVer(usuario, cliente.cliente.empresaId, cliente.cliente.vendedorId)) {
                    throw ErrorServidor(403, "No tienes permiso sobre este cliente")
                }
                val guardado = cliente.copy(cliente = cliente.cliente.copy(pendienteSync = false))
                val indice = bd.clientes.indexOfFirst { it.cliente.id == id }
                if (indice >= 0) bd.clientes[indice] = guardado else bd.clientes += guardado
                return guardado
            }
            "DELETE" -> {
                val cliente = bd.clientes.find { it.cliente.id == id } ?: return null
                if (!puedeVer(usuario, cliente.cliente.empresaId, cliente.cliente.vendedorId)) {
                    throw ErrorServidor(403, "No tienes permiso sobre este cliente")
                }
                if (bd.cotizaciones.any { it.cotizacion.clienteId == id }) {
                    throw ErrorServidor(409, "El cliente tiene cotizaciones y no se puede eliminar")
                }
                bd.clientes.remove(cliente)
                return null
            }
        }
        throw ErrorServidor(405, "Método no permitido")
    }

    // ---------------- Cotizaciones ----------------

    private fun cotizaciones(metodo: String, id: String?, accion: String?, cuerpo: String?, usuario: UsuarioEntity): Any? {
        actualizarEstadosAutomaticos()
        when {
            metodo == "GET" ->
                return bd.cotizaciones.filter { puedeVer(usuario, it.cotizacion.empresaId, it.cotizacion.vendedorId) }
            metodo == "PUT" -> return guardarCotizacion(id!!, gson.fromJson(cuerpo, CotizacionConItems::class.java), usuario)
            metodo == "POST" && accion == "simular-respuesta" -> return simularRespuesta(id!!, usuario)
        }
        throw ErrorServidor(405, "Método no permitido")
    }

    private fun guardarCotizacion(id: String, recibida: CotizacionConItems, usuario: UsuarioEntity): CotizacionConItems {
        if (usuario.rol == Rol.ADMINISTRADOR) throw ErrorServidor(403, "El administrador solo puede consultar cotizaciones")
        val nueva = recibida.cotizacion
        if (!puedeVer(usuario, nueva.empresaId, nueva.vendedorId)) throw ErrorServidor(403, "No tienes permiso sobre esta cotización")

        val indice = bd.cotizaciones.indexOfFirst { it.cotizacion.id == id }
        val existente = bd.cotizaciones.getOrNull(indice)

        // Una cotización ya enviada no cambia su contenido: solo puede anularse o ser reemplazada
        if (existente != null && existente.cotizacion.estado != EstadoCotizacion.BORRADOR) {
            val permitido = nueva.estado == EstadoCotizacion.ANULADA || nueva.estado == EstadoCotizacion.REEMPLAZADA
            if (!permitido) return existente
            val actualizada = existente.copy(cotizacion = existente.cotizacion.copy(estado = nueva.estado))
            bd.cotizaciones[indice] = actualizada
            return actualizada
        }

        var cotizacion = nueva.copy(pendienteSync = false, envioPendiente = false)

        // Número correlativo por empresa (las versiones nuevas conservan el número original)
        if (cotizacion.numero.startsWith("TMP-")) {
            val origen = bd.cotizaciones.find { it.cotizacion.id == cotizacion.cotizacionOrigenId }
            val numero = origen?.cotizacion?.numero ?: run {
                val siguiente = (bd.correlativos[usuario.empresaId] ?: 0) + 1
                bd.correlativos[usuario.empresaId] = siguiente
                "COT-%04d".format(siguiente)
            }
            cotizacion = cotizacion.copy(numero = numero)
        }

        // Al enviarse quedan registradas la fecha de envío y la de vencimiento
        if (cotizacion.estado == EstadoCotizacion.ENVIADA) {
            val ahora = System.currentTimeMillis()
            cotizacion = cotizacion.copy(
                fechaEnvio = ahora,
                fechaVencimiento = ahora + cotizacion.diasValidez * DIA
            )
        }

        // La versión anterior queda reemplazada
        cotizacion.cotizacionOrigenId?.let { origenId ->
            val iOrigen = bd.cotizaciones.indexOfFirst { it.cotizacion.id == origenId }
            if (iOrigen >= 0) {
                val origen = bd.cotizaciones[iOrigen]
                bd.cotizaciones[iOrigen] = origen.copy(cotizacion = origen.cotizacion.copy(estado = EstadoCotizacion.REEMPLAZADA))
            }
        }

        val guardada = CotizacionConItems(cotizacion, recibida.items)
        if (indice >= 0) bd.cotizaciones[indice] = guardada else bd.cotizaciones += guardada
        return guardada
    }

    private fun simularRespuesta(id: String, usuario: UsuarioEntity): CotizacionConItems {
        val indice = bd.cotizaciones.indexOfFirst { it.cotizacion.id == id }
        val actual = bd.cotizaciones.getOrNull(indice) ?: throw ErrorServidor(404, "Cotización no encontrada")
        if (!puedeVer(usuario, actual.cotizacion.empresaId, actual.cotizacion.vendedorId)) {
            throw ErrorServidor(403, "No tienes permiso sobre esta cotización")
        }
        if (actual.cotizacion.estado != EstadoCotizacion.ENVIADA) {
            throw ErrorServidor(409, "Solo una cotización enviada puede recibir respuesta")
        }
        val respondida = actual.copy(cotizacion = responder(actual.cotizacion))
        bd.cotizaciones[indice] = respondida
        return respondida
    }

    // El cliente responde en la página pública (70% acepta, 30% rechaza)
    private fun responder(cotizacion: cl.duoc.tratohecho.data.local.entity.CotizacionEntity) =
        cotizacion.copy(
            estado = if (Random.nextInt(100) < 70) EstadoCotizacion.ACEPTADA else EstadoCotizacion.RECHAZADA,
            fechaRespuesta = System.currentTimeMillis()
        )

    // Simula lo que ocurre en la plataforma mientras la app no está mirando:
    // - las enviadas que pasaron su fecha de validez quedan vencidas
    // - las enviadas desde la app reciben respuesta del cliente después de unos minutos
    private fun actualizarEstadosAutomaticos() {
        val ahora = System.currentTimeMillis()
        var huboCambios = false
        bd.cotizaciones.forEachIndexed { i, item ->
            val cot = item.cotizacion
            if (cot.estado != EstadoCotizacion.ENVIADA) return@forEachIndexed
            val envio = cot.fechaEnvio ?: return@forEachIndexed
            val nuevo = when {
                cot.fechaVencimiento != null && ahora > cot.fechaVencimiento ->
                    cot.copy(estado = EstadoCotizacion.VENCIDA)
                envio > bd.fechaSemilla && ahora - envio > TIEMPO_RESPUESTA_CLIENTE ->
                    responder(cot)
                else -> null
            }
            if (nuevo != null) {
                bd.cotizaciones[i] = item.copy(cotizacion = nuevo)
                huboCambios = true
            }
        }
        if (huboCambios) guardar()
    }

    // ---------------- Persistencia del servidor ----------------

    private fun cargar(): BaseDatosServidor {
        if (archivo.exists()) {
            try {
                return gson.fromJson(archivo.readText(), BaseDatosServidor::class.java)
            } catch (e: Exception) {
                // Si el archivo está dañado se vuelve a crear con los datos de prueba
            }
        }
        return DatosSemilla.crear(System.currentTimeMillis()).also { guardarEn(it) }
    }

    private fun guardar() = guardarEn(bd)

    private fun guardarEn(datos: BaseDatosServidor) {
        archivo.writeText(gson.toJson(datos))
    }

    companion object {
        private const val DIA = 24L * 60 * 60 * 1000
        // Tiempo que "demora" el cliente en responder una cotización enviada desde la app
        const val TIEMPO_RESPUESTA_CLIENTE = 2L * 60 * 1000
    }
}
