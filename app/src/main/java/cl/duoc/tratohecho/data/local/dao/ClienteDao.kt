package cl.duoc.tratohecho.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import cl.duoc.tratohecho.data.local.entity.ClienteConContactos
import cl.duoc.tratohecho.data.local.entity.ClienteEntity
import cl.duoc.tratohecho.data.local.entity.ContactoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClienteDao {
    @Transaction
    @Query("SELECT * FROM clientes ORDER BY razonSocial")
    fun observarClientes(): Flow<List<ClienteConContactos>>

    @Transaction
    @Query("SELECT * FROM clientes WHERE id = :id")
    fun observarCliente(id: String): Flow<ClienteConContactos?>

    @Transaction
    @Query("SELECT * FROM clientes WHERE id = :id")
    suspend fun obtenerCliente(id: String): ClienteConContactos?

    @Transaction
    @Query("SELECT * FROM clientes WHERE pendienteSync = 1")
    suspend fun obtenerPendientes(): List<ClienteConContactos>

    @Query("SELECT COUNT(*) FROM clientes WHERE pendienteSync = 1")
    fun contarPendientes(): Flow<Int>

    @Query("SELECT id FROM clientes WHERE pendienteSync = 1")
    suspend fun idsPendientes(): List<String>

    @Query("SELECT COUNT(*) FROM clientes WHERE rut = :rut AND id != :idExcluido")
    suspend fun contarPorRut(rut: String, idExcluido: String): Int

    @Upsert
    suspend fun guardarCliente(cliente: ClienteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarContactos(contactos: List<ContactoEntity>)

    @Query("DELETE FROM contactos WHERE clienteId = :clienteId")
    suspend fun eliminarContactosDe(clienteId: String)

    @Query("DELETE FROM clientes WHERE id = :id")
    suspend fun eliminar(id: String)

    @Query("UPDATE clientes SET pendienteSync = 0 WHERE id = :id")
    suspend fun marcarSincronizado(id: String)

    @Query("DELETE FROM clientes WHERE pendienteSync = 0")
    suspend fun eliminarSincronizados()

    // Guarda el cliente junto con su lista completa de contactos
    @Transaction
    suspend fun guardarConContactos(cliente: ClienteEntity, contactos: List<ContactoEntity>) {
        guardarCliente(cliente)
        eliminarContactosDe(cliente.id)
        insertarContactos(contactos)
    }

    // Reemplaza los clientes ya sincronizados con los de la API, sin tocar los pendientes
    @Transaction
    suspend fun reemplazarSincronizados(clientes: List<ClienteConContactos>) {
        val pendientes = idsPendientes()
        eliminarSincronizados()
        clientes.filter { it.cliente.id !in pendientes }.forEach {
            guardarConContactos(it.cliente.copy(pendienteSync = false), it.contactos)
        }
    }
}
