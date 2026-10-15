package cl.duoc.tratohecho.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import cl.duoc.tratohecho.data.local.entity.CotizacionConItems
import cl.duoc.tratohecho.data.local.entity.CotizacionEntity
import cl.duoc.tratohecho.data.local.entity.ItemCotizacionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CotizacionDao {
    @Query("SELECT * FROM cotizaciones ORDER BY fechaCreacion DESC")
    fun observarCotizaciones(): Flow<List<CotizacionEntity>>

    @Transaction
    @Query("SELECT * FROM cotizaciones WHERE id = :id")
    fun observarCotizacion(id: String): Flow<CotizacionConItems?>

    @Transaction
    @Query("SELECT * FROM cotizaciones WHERE id = :id")
    suspend fun obtenerCotizacion(id: String): CotizacionConItems?

    @Query("SELECT * FROM cotizaciones")
    suspend fun obtenerTodas(): List<CotizacionEntity>

    @Transaction
    @Query("SELECT * FROM cotizaciones WHERE pendienteSync = 1 ORDER BY fechaCreacion")
    suspend fun obtenerPendientes(): List<CotizacionConItems>

    @Query("SELECT COUNT(*) FROM cotizaciones WHERE pendienteSync = 1")
    fun contarPendientes(): Flow<Int>

    @Query("SELECT id FROM cotizaciones WHERE pendienteSync = 1")
    suspend fun idsPendientes(): List<String>

    @Query("SELECT COUNT(*) FROM cotizaciones WHERE clienteId = :clienteId")
    suspend fun contarPorCliente(clienteId: String): Int

    @Upsert
    suspend fun guardarCotizacion(cotizacion: CotizacionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarItems(items: List<ItemCotizacionEntity>)

    @Query("DELETE FROM items_cotizacion WHERE cotizacionId = :cotizacionId")
    suspend fun eliminarItemsDe(cotizacionId: String)

    @Query("DELETE FROM cotizaciones WHERE id = :id")
    suspend fun eliminar(id: String)

    @Query("DELETE FROM cotizaciones WHERE pendienteSync = 0")
    suspend fun eliminarSincronizadas()

    // Guarda la cotización junto con todos sus ítems
    @Transaction
    suspend fun guardarConItems(cotizacion: CotizacionEntity, items: List<ItemCotizacionEntity>) {
        guardarCotizacion(cotizacion)
        eliminarItemsDe(cotizacion.id)
        insertarItems(items)
    }

    // Reemplaza las cotizaciones sincronizadas con las de la API, sin tocar las pendientes
    @Transaction
    suspend fun reemplazarSincronizadas(cotizaciones: List<CotizacionConItems>) {
        val pendientes = idsPendientes()
        eliminarSincronizadas()
        cotizaciones.filter { it.cotizacion.id !in pendientes }.forEach {
            guardarConItems(it.cotizacion.copy(pendienteSync = false, envioPendiente = false), it.items)
        }
    }
}
