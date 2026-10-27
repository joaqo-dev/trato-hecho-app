package cl.duoc.tratohecho.servidor.datos

import com.google.gson.Gson
import java.sql.Connection
import java.sql.DriverManager

// Guarda los datos del servidor en una base de datos por JDBC:
// - PostgreSQL en producción (contenedor "db" del compose.yml)
// - SQLite en desarrollo (un archivo, por defecto backend/tratohecho.db)
// Cada tabla tiene el id, la empresa a la que pertenece y el registro completo en JSON
// (JSONB en PostgreSQL), así se puede revisar con cualquier cliente de base de datos.
class AlmacenJdbc(
    private val url: String,
    private val usuario: String? = null,
    private val clave: String? = null
) {
    private val gson = Gson()
    private val esPostgres = url.startsWith("jdbc:postgresql:")
    private val tipoJson = if (esPostgres) "JSONB" else "TEXT"
    private val valorJson = if (esPostgres) "?::jsonb" else "?"
    private var conexion: Connection? = null

    init {
        conectar().createStatement().use { st ->
            TABLAS.forEach { tabla ->
                st.executeUpdate("CREATE TABLE IF NOT EXISTS $tabla (id TEXT PRIMARY KEY, empresa_id TEXT NOT NULL, datos $tipoJson NOT NULL)")
            }
            st.executeUpdate("CREATE TABLE IF NOT EXISTS correlativos (empresa_id TEXT PRIMARY KEY, ultimo INTEGER NOT NULL)")
            st.executeUpdate("CREATE TABLE IF NOT EXISTS sesiones (token TEXT PRIMARY KEY, usuario_id TEXT NOT NULL, creada BIGINT NOT NULL)")
        }
    }

    // Reutiliza la conexión y la vuelve a abrir si se cortó (ej: se reinició el contenedor de la base)
    @Synchronized
    private fun conectar(): Connection {
        conexion?.takeIf { !it.isClosed && it.isValid(2) }?.let { return it }
        return DriverManager.getConnection(url, usuario, clave).also { conexion = it }
    }

    // Para el endpoint /salud
    fun disponible(): Boolean = try {
        conectar().isValid(2)
    } catch (e: Exception) {
        false
    }

    // null = base vacía (primera vez): el servidor la llena con los datos de prueba
    @Synchronized
    fun cargar(): BaseDatosServidor? {
        if (leerJson("empresas").isEmpty()) return null
        val bd = BaseDatosServidor()
        bd.empresas += leerComo("empresas")
        bd.usuarios += leerComo<UsuarioServidor>("usuarios")
        bd.unidades += leerComo("unidades")
        bd.impuestos += leerComo("impuestos")
        bd.productos += leerComo("productos")
        bd.clientes += leerComo("clientes")
        bd.cotizaciones += leerComo("cotizaciones")
        conectar().createStatement().use { st ->
            st.executeQuery("SELECT empresa_id, ultimo FROM correlativos").use { rs ->
                while (rs.next()) bd.correlativos[rs.getString(1)] = rs.getInt(2)
            }
        }
        return bd
    }

    // Reescribe todo dentro de una transacción (son pocos datos, así se mantiene simple)
    @Synchronized
    fun guardar(bd: BaseDatosServidor) {
        val c = conectar()
        c.autoCommit = false
        try {
            escribir(c, "empresas", bd.empresas) { it.id to it.id }
            escribir(c, "usuarios", bd.usuarios) { it.usuario.id to it.usuario.empresaId }
            escribir(c, "unidades", bd.unidades) { it.id to it.empresaId }
            escribir(c, "impuestos", bd.impuestos) { it.id to it.empresaId }
            escribir(c, "productos", bd.productos) { it.id to it.empresaId }
            escribir(c, "clientes", bd.clientes) { it.cliente.id to it.cliente.empresaId }
            escribir(c, "cotizaciones", bd.cotizaciones) { it.cotizacion.id to it.cotizacion.empresaId }
            c.createStatement().use { it.executeUpdate("DELETE FROM correlativos") }
            c.prepareStatement("INSERT INTO correlativos (empresa_id, ultimo) VALUES (?, ?)").use { ps ->
                bd.correlativos.forEach { (empresa, ultimo) ->
                    ps.setString(1, empresa)
                    ps.setInt(2, ultimo)
                    ps.addBatch()
                }
                ps.executeBatch()
            }
            c.commit()
        } catch (e: Exception) {
            c.rollback()
            throw e
        } finally {
            c.autoCommit = true
        }
    }

    // ---------------- Sesiones de la app (token aleatorio → usuario) ----------------

    @Synchronized
    fun cargarSesiones(): Map<String, String> {
        val sesiones = mutableMapOf<String, String>()
        conectar().createStatement().use { st ->
            st.executeQuery("SELECT token, usuario_id FROM sesiones").use { rs ->
                while (rs.next()) sesiones[rs.getString(1)] = rs.getString(2)
            }
        }
        return sesiones
    }

    @Synchronized
    fun guardarSesion(token: String, usuarioId: String) {
        conectar().prepareStatement("INSERT INTO sesiones (token, usuario_id, creada) VALUES (?, ?, ?)").use { ps ->
            ps.setString(1, token)
            ps.setString(2, usuarioId)
            ps.setLong(3, System.currentTimeMillis())
            ps.executeUpdate()
        }
    }

    @Synchronized
    fun borrarSesiones() {
        conectar().createStatement().use { it.executeUpdate("DELETE FROM sesiones") }
    }

    private fun <T> escribir(c: Connection, tabla: String, lista: List<T>, claves: (T) -> Pair<String, String>) {
        c.createStatement().use { it.executeUpdate("DELETE FROM $tabla") }
        c.prepareStatement("INSERT INTO $tabla (id, empresa_id, datos) VALUES (?, ?, $valorJson)").use { ps ->
            lista.forEach { elemento ->
                val (id, empresa) = claves(elemento)
                ps.setString(1, id)
                ps.setString(2, empresa)
                ps.setString(3, gson.toJson(elemento))
                ps.addBatch()
            }
            ps.executeBatch()
        }
    }

    private inline fun <reified T> leerComo(tabla: String): List<T> = leerJson(tabla).map { gson.fromJson(it, T::class.java) }

    private fun leerJson(tabla: String): List<String> {
        val lista = mutableListOf<String>()
        conectar().createStatement().use { st ->
            st.executeQuery("SELECT datos FROM $tabla").use { rs ->
                while (rs.next()) lista += rs.getString(1)
            }
        }
        return lista
    }

    companion object {
        private val TABLAS = listOf("empresas", "usuarios", "unidades", "impuestos", "productos", "clientes", "cotizaciones")
    }
}
