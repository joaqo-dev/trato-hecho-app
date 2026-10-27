package cl.duoc.tratohecho.servidor

import cl.duoc.tratohecho.servidor.datos.AlmacenJdbc
import cl.duoc.tratohecho.servidor.datos.BaseDatosServidor
import cl.duoc.tratohecho.servidor.datos.DatosSemilla
import cl.duoc.tratohecho.servidor.datos.UsuarioServidor
import cl.duoc.tratohecho.servidor.modelo.ClienteConContactos
import cl.duoc.tratohecho.servidor.modelo.CotizacionConItems
import cl.duoc.tratohecho.servidor.modelo.CotizacionEntity
import cl.duoc.tratohecho.servidor.modelo.EmpresaEntity
import cl.duoc.tratohecho.servidor.modelo.EstadoCotizacion
import cl.duoc.tratohecho.servidor.modelo.ImpuestoEntity
import cl.duoc.tratohecho.servidor.modelo.LoginRequest
import cl.duoc.tratohecho.servidor.modelo.LoginResponse
import cl.duoc.tratohecho.servidor.modelo.ProductoEntity
import cl.duoc.tratohecho.servidor.modelo.ReasignarClienteRequest
import cl.duoc.tratohecho.servidor.modelo.Rol
import cl.duoc.tratohecho.servidor.modelo.UnidadEntity
import cl.duoc.tratohecho.servidor.modelo.UsuarioEntity
import cl.duoc.tratohecho.servidor.modelo.UsuarioRequest
import com.google.gson.Gson
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

// Error que el servidor devuelve como respuesta HTTP (ej: 401, 403, 404)
class ErrorServidor(val codigo: Int, mensaje: String) : Exception(mensaje)

// Backend de Trato Hecho. Aplica las reglas del servidor:
// - Cada usuario solo ve los datos de su empresa (multi-tenant).
// - Un vendedor solo ve sus clientes y cotizaciones; supervisor y administrador ven todo.
// - Asigna el número correlativo de cada cotización.
// - Marca como vencidas las cotizaciones enviadas que pasaron su validez.
// La respuesta del cliente y la facturación se registran desde el panel web (ver PanelRutas).
class ServidorTratoHecho(private val almacen: AlmacenJdbc) {

    private val gson = Gson()
    private val bd: BaseDatosServidor = almacen.cargar()
        ?: DatosSemilla.crear(System.currentTimeMillis()).also { almacen.guardar(it) }

    // Sesiones abiertas: token aleatorio → id del usuario (se guardan en la base para sobrevivir reinicios)
    private val sesiones = ConcurrentHashMap(almacen.cargarSesiones())

    // Última vez que cada usuario habló con la API (lo muestra el panel para ver quién está conectado)
    private val ultimaActividad = ConcurrentHashMap<String, Long>()

    @Synchronized
    fun atender(metodo: String, ruta: List<String>, cuerpo: String?, token: String?): Any? {
        if (ruta == listOf("auth", "login") && metodo == "POST") {
            return login(gson.fromJson(cuerpo, LoginRequest::class.java))
        }
        val usuario = usuarioDesdeToken(token)
        ultimaActividad[usuario.id] = System.currentTimeMillis()
        val resultado: Any? = when (ruta.firstOrNull()) {
            "empresa" -> empresa(metodo, cuerpo, usuario)
            "usuarios" -> if (ruta.getOrNull(2) == "traspasar-clientes") traspasarClientes(ruta[1], cuerpo, usuario)
            else usuarios(metodo, ruta.getOrNull(1), cuerpo, usuario)
            "vendedores" -> vendedores(usuario)
            "unidades" -> catalogo(metodo, ruta.getOrNull(1), cuerpo, usuario, bd.unidades, UnidadEntity::class.java) { it.id }
            "impuestos" -> catalogo(metodo, ruta.getOrNull(1), cuerpo, usuario, bd.impuestos, ImpuestoEntity::class.java) { it.id }
            "productos" -> catalogo(metodo, ruta.getOrNull(1), cuerpo, usuario, bd.productos, ProductoEntity::class.java) { it.id }
            "clientes" -> if (ruta.getOrNull(2) == "reasignar") reasignarCliente(ruta[1], cuerpo, usuario)
            else clientes(metodo, ruta.getOrNull(1), cuerpo, usuario)
            "cotizaciones" -> cotizaciones(metodo, ruta.getOrNull(1), cuerpo, usuario)
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
        ultimaActividad[encontrado.usuario.id] = System.currentTimeMillis()
        val token = UUID.randomUUID().toString() + UUID.randomUUID().toString().replace("-", "")
        sesiones[token] = encontrado.usuario.id
        almacen.guardarSesion(token, encontrado.usuario.id)
        return LoginResponse(token = token, usuario = encontrado.usuario, empresa = empresa)
    }

    private fun usuarioDesdeToken(token: String?): UsuarioEntity {
        val id = token?.removePrefix("Bearer ")?.let { sesiones[it] }
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
                    // Un vendedor que deja de serlo (baja o cambio de rol) no puede quedar con clientes a cargo
                    val anterior = bd.usuarios[indice].usuario
                    val dejaDeVender = anterior.rol == Rol.VENDEDOR && (!nuevo.activo || nuevo.rol != Rol.VENDEDOR)
                    val clientesACargo = bd.clientes.count { it.cliente.vendedorId == id && it.cliente.activo }
                    if (dejaDeVender && clientesACargo > 0) {
                        throw ErrorServidor(409, "${anterior.nombre} tiene $clientesACargo cliente(s) a cargo. Reasígnalos antes de darlo de baja.")
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
            // Nunca se borran usuarios: se dan de baja (activo = false) para conservar su historial
            "DELETE" -> throw ErrorServidor(405, "Los usuarios no se eliminan; se dan de baja desactivándolos")
        }
        throw ErrorServidor(405, "Método no permitido")
    }

    // Vendedores activos de la empresa (para reasignar clientes)
    private fun vendedores(usuario: UsuarioEntity): List<UsuarioEntity> {
        if (usuario.rol == Rol.VENDEDOR) throw ErrorServidor(403, "No tienes permiso para ver los vendedores")
        return bd.usuarios.map { it.usuario }
            .filter { it.empresaId == usuario.empresaId && it.rol == Rol.VENDEDOR && it.activo }
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

    // El vendedor solo ve sus clientes activos; supervisor y administrador ven todos los de la empresa
    private fun clientes(metodo: String, id: String?, cuerpo: String?, usuario: UsuarioEntity): Any? {
        when (metodo) {
            "GET" -> return bd.clientes.filter {
                puedeVer(usuario, it.cliente.empresaId, it.cliente.vendedorId) &&
                    (usuario.rol != Rol.VENDEDOR || it.cliente.activo)
            }
            "PUT" -> {
                val recibido = gson.fromJson(cuerpo, ClienteConContactos::class.java)
                if (recibido.cliente.empresaId != usuario.empresaId) throw ErrorServidor(403, "No pertenece a tu empresa")
                val indice = bd.clientes.indexOfFirst { it.cliente.id == id }
                val existente = bd.clientes.getOrNull(indice)

                val guardado = when (usuario.rol) {
                    // Solo el administrador crea, modifica, asigna vendedor y da de baja clientes
                    Rol.ADMINISTRADOR -> {
                        val vendedor = bd.usuarios.map { it.usuario }.find {
                            it.id == recibido.cliente.vendedorId && it.empresaId == usuario.empresaId && it.rol == Rol.VENDEDOR
                        } ?: throw ErrorServidor(400, "El cliente debe tener un vendedor asignado")
                        recibido.copy(cliente = recibido.cliente.copy(vendedorNombre = vendedor.nombre, pendienteSync = false))
                    }
                    // El vendedor solo actualiza la información de contacto de sus propios clientes
                    Rol.VENDEDOR -> {
                        if (existente == null) throw ErrorServidor(403, "Solo el administrador puede registrar clientes")
                        // Si se lo reasignaron mientras estaba sin conexión, su cambio se descarta
                        if (existente.cliente.vendedorId != usuario.id) return existente
                        existente.copy(
                            cliente = existente.cliente.copy(correo = recibido.cliente.correo, telefono = recibido.cliente.telefono),
                            contactos = recibido.contactos
                        )
                    }
                    Rol.SUPERVISOR -> throw ErrorServidor(403, "El supervisor solo puede consultar clientes")
                }
                if (indice >= 0) bd.clientes[indice] = guardado else bd.clientes += guardado
                return guardado
            }
            // Nunca se borran clientes: el administrador los da de baja (activo = false) y su historial se conserva
            "DELETE" -> throw ErrorServidor(405, "Los clientes no se eliminan; se dan de baja")
        }
        throw ErrorServidor(405, "Método no permitido")
    }

    // Cambia el vendedor a cargo. Las cotizaciones existentes siguen siendo de quien las hizo.
    private fun reasignarCliente(id: String, cuerpo: String?, usuario: UsuarioEntity): ClienteConContactos {
        exigirAdministrador(usuario)
        val request = gson.fromJson(cuerpo, ReasignarClienteRequest::class.java)
        val indice = bd.clientes.indexOfFirst { it.cliente.id == id && it.cliente.empresaId == usuario.empresaId }
        if (indice < 0) throw ErrorServidor(404, "Cliente no encontrado")
        val vendedor = vendedores(usuario).find { it.id == request.vendedorId }
            ?: throw ErrorServidor(400, "El vendedor no existe o está desactivado")
        val actual = bd.clientes[indice]
        val reasignado = actual.copy(
            cliente = actual.cliente.copy(vendedorId = vendedor.id, vendedorNombre = vendedor.nombre)
        )
        bd.clientes[indice] = reasignado
        return reasignado
    }

    // Traspasa todos los clientes activos de un vendedor a otro (se usa antes de darlo de baja)
    private fun traspasarClientes(id: String, cuerpo: String?, usuario: UsuarioEntity): Int {
        exigirAdministrador(usuario)
        val request = gson.fromJson(cuerpo, ReasignarClienteRequest::class.java)
        if (request.vendedorId == id) throw ErrorServidor(400, "Elige otro vendedor")
        val vendedor = vendedores(usuario).find { it.id == request.vendedorId }
            ?: throw ErrorServidor(400, "El vendedor no existe o está desactivado")
        var cantidad = 0
        bd.clientes.forEachIndexed { i, c ->
            if (c.cliente.empresaId == usuario.empresaId && c.cliente.vendedorId == id && c.cliente.activo) {
                bd.clientes[i] = c.copy(cliente = c.cliente.copy(vendedorId = vendedor.id, vendedorNombre = vendedor.nombre))
                cantidad++
            }
        }
        return cantidad
    }

    // ---------------- Cotizaciones ----------------

    private fun cotizaciones(metodo: String, id: String?, cuerpo: String?, usuario: UsuarioEntity): Any? {
        marcarVencidas()
        when (metodo) {
            "GET" -> return bd.cotizaciones.filter { puedeVer(usuario, it.cotizacion.empresaId, it.cotizacion.vendedorId) }
            "PUT" -> return guardarCotizacion(id!!, gson.fromJson(cuerpo, CotizacionConItems::class.java), usuario)
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
            // Si el cliente la aceptó mientras el vendedor la anulaba sin señal, gana la aceptación
            val yaAceptada = nueva.estado == EstadoCotizacion.ANULADA && existente.cotizacion.estado in ESTADOS_ACEPTADOS
            if (!permitido || yaAceptada) return existente
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

    // Las enviadas que pasaron su fecha de validez sin respuesta quedan vencidas
    private fun marcarVencidas() {
        val ahora = System.currentTimeMillis()
        var huboCambios = false
        bd.cotizaciones.forEachIndexed { i, item ->
            val cot = item.cotizacion
            if (cot.estado == EstadoCotizacion.ENVIADA && cot.fechaVencimiento != null && ahora > cot.fechaVencimiento) {
                bd.cotizaciones[i] = item.copy(cotizacion = cot.copy(estado = EstadoCotizacion.VENCIDA))
                huboCambios = true
            }
        }
        if (huboCambios) guardar()
    }

    // ---------------- Panel web (simulación del cliente y de facturación) ----------------

    @Synchronized
    fun panelEmpresas(): List<Map<String, Any>> {
        marcarVencidas()
        return bd.empresas.map { empresa ->
            val cotizaciones = bd.cotizaciones.filter { it.cotizacion.empresaId == empresa.id }
            mapOf(
                "empresa" to empresa,
                "clientes" to bd.clientes.count { it.cliente.empresaId == empresa.id },
                "cotizaciones" to cotizaciones.size,
                "porResponder" to cotizaciones.count { it.cotizacion.estado == EstadoCotizacion.ENVIADA },
                "porFacturar" to cotizaciones.count { it.cotizacion.estado == EstadoCotizacion.ACEPTADA }
            )
        }
    }

    @Synchronized
    fun panelEmpresa(empresaId: String): Map<String, Any> {
        marcarVencidas()
        val empresa = bd.empresas.find { it.id == empresaId } ?: throw ErrorServidor(404, "Empresa no encontrada")
        return mapOf(
            "empresa" to empresa,
            "usuarios" to bd.usuarios.filter { it.usuario.empresaId == empresaId }.map {
                mapOf("usuario" to it.usuario, "ultimaActividad" to ultimaActividad[it.usuario.id])
            },
            "clientes" to bd.clientes.filter { it.cliente.empresaId == empresaId }.map { it.cliente },
            "cotizaciones" to bd.cotizaciones.filter { it.cotizacion.empresaId == empresaId }
                .sortedByDescending { it.cotizacion.fechaEnvio ?: it.cotizacion.fechaCreacion },
            "ahora" to System.currentTimeMillis()
        )
    }

    // El cliente acepta o rechaza en la página pública de Trato Hecho
    @Synchronized
    fun responderCotizacion(id: String, aceptada: Boolean): CotizacionConItems =
        cambiarEstado(id, EstadoCotizacion.ENVIADA, "Solo una cotización enviada puede recibir respuesta") {
            it.copy(
                estado = if (aceptada) EstadoCotizacion.ACEPTADA else EstadoCotizacion.RECHAZADA,
                fechaRespuesta = System.currentTimeMillis()
            )
        }

    // La empresa registra la factura de una cotización aceptada
    @Synchronized
    fun facturarCotizacion(id: String): CotizacionConItems =
        cambiarEstado(id, EstadoCotizacion.ACEPTADA, "Solo una cotización aceptada se puede facturar") {
            it.copy(estado = EstadoCotizacion.FACTURADA, fechaFacturacion = System.currentTimeMillis())
        }

    private fun cambiarEstado(
        id: String,
        estadoRequerido: EstadoCotizacion,
        mensajeError: String,
        cambio: (CotizacionEntity) -> CotizacionEntity
    ): CotizacionConItems {
        marcarVencidas()
        val indice = bd.cotizaciones.indexOfFirst { it.cotizacion.id == id }
        val actual = bd.cotizaciones.getOrNull(indice) ?: throw ErrorServidor(404, "Cotización no encontrada")
        if (actual.cotizacion.estado != estadoRequerido) throw ErrorServidor(409, mensajeError)
        val nueva = actual.copy(cotizacion = cambio(actual.cotizacion))
        bd.cotizaciones[indice] = nueva
        guardar()
        return nueva
    }

    // Vuelve a los datos de prueba iniciales (botón del panel)
    @Synchronized
    fun reiniciarDatos() {
        val semilla = DatosSemilla.crear(System.currentTimeMillis())
        bd.empresas.apply { clear(); addAll(semilla.empresas) }
        bd.usuarios.apply { clear(); addAll(semilla.usuarios) }
        bd.unidades.apply { clear(); addAll(semilla.unidades) }
        bd.impuestos.apply { clear(); addAll(semilla.impuestos) }
        bd.productos.apply { clear(); addAll(semilla.productos) }
        bd.clientes.apply { clear(); addAll(semilla.clientes) }
        bd.cotizaciones.apply { clear(); addAll(semilla.cotizaciones) }
        bd.correlativos.apply { clear(); putAll(semilla.correlativos) }
        guardar()
        // Las sesiones abiertas dejan de valer: la app vuelve al login
        sesiones.clear()
        almacen.borrarSesiones()
    }

    // Nombre del usuario dueño de un token (para el registro de peticiones)
    fun usuarioDeToken(token: String?): String? = token?.removePrefix("Bearer ")?.let { sesiones[it] }

    fun baseDisponible(): Boolean = almacen.disponible()

    private fun guardar() = almacen.guardar(bd)

    companion object {
        private const val DIA = 24L * 60 * 60 * 1000
        private val ESTADOS_ACEPTADOS = setOf(EstadoCotizacion.ACEPTADA, EstadoCotizacion.FACTURADA)
    }
}
