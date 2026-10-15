package cl.duoc.tratohecho.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import cl.duoc.tratohecho.data.local.entity.ImpuestoEntity
import cl.duoc.tratohecho.data.local.entity.ProductoEntity
import cl.duoc.tratohecho.data.local.entity.UnidadEntity
import kotlinx.coroutines.flow.Flow

// Productos, tipos de unidad e impuestos de la empresa
@Dao
interface CatalogoDao {

    // ---------- Productos ----------
    @Query("SELECT * FROM productos ORDER BY nombre")
    fun observarProductos(): Flow<List<ProductoEntity>>

    @Query("SELECT * FROM productos WHERE id = :id")
    suspend fun obtenerProducto(id: String): ProductoEntity?

    @Upsert
    suspend fun guardarProducto(producto: ProductoEntity)

    @Query("DELETE FROM productos WHERE id = :id")
    suspend fun eliminarProducto(id: String)

    @Query("DELETE FROM productos")
    suspend fun eliminarProductos()

    @Insert
    suspend fun insertarProductos(productos: List<ProductoEntity>)

    // ---------- Unidades ----------
    @Query("SELECT * FROM unidades ORDER BY nombre")
    fun observarUnidades(): Flow<List<UnidadEntity>>

    @Query("SELECT * FROM unidades WHERE id = :id")
    suspend fun obtenerUnidad(id: String): UnidadEntity?

    @Upsert
    suspend fun guardarUnidad(unidad: UnidadEntity)

    @Query("DELETE FROM unidades WHERE id = :id")
    suspend fun eliminarUnidad(id: String)

    @Query("DELETE FROM unidades")
    suspend fun eliminarUnidades()

    @Insert
    suspend fun insertarUnidades(unidades: List<UnidadEntity>)

    // ---------- Impuestos ----------
    @Query("SELECT * FROM impuestos ORDER BY porcentaje DESC")
    fun observarImpuestos(): Flow<List<ImpuestoEntity>>

    @Query("SELECT * FROM impuestos WHERE id = :id")
    suspend fun obtenerImpuesto(id: String): ImpuestoEntity?

    @Upsert
    suspend fun guardarImpuesto(impuesto: ImpuestoEntity)

    @Query("DELETE FROM impuestos WHERE id = :id")
    suspend fun eliminarImpuesto(id: String)

    @Query("DELETE FROM impuestos")
    suspend fun eliminarImpuestos()

    @Insert
    suspend fun insertarImpuestos(impuestos: List<ImpuestoEntity>)

    // Reemplaza todo el catálogo con lo que viene de la API
    @Transaction
    suspend fun reemplazarCatalogo(
        unidades: List<UnidadEntity>,
        impuestos: List<ImpuestoEntity>,
        productos: List<ProductoEntity>
    ) {
        eliminarProductos()
        eliminarUnidades()
        eliminarImpuestos()
        insertarUnidades(unidades)
        insertarImpuestos(impuestos)
        insertarProductos(productos)
    }
}
