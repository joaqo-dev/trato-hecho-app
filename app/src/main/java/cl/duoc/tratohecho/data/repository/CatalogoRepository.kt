package cl.duoc.tratohecho.data.repository

import cl.duoc.tratohecho.data.local.dao.CatalogoDao
import cl.duoc.tratohecho.data.local.entity.ImpuestoEntity
import cl.duoc.tratohecho.data.local.entity.ProductoEntity
import cl.duoc.tratohecho.data.local.entity.UnidadEntity
import cl.duoc.tratohecho.data.remote.TratoHechoApi
import cl.duoc.tratohecho.data.remote.llamarApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

// El catálogo se lee desde Room (funciona sin conexión) y solo el administrador lo modifica en la API
@Singleton
class CatalogoRepository @Inject constructor(
    private val api: TratoHechoApi,
    private val catalogoDao: CatalogoDao
) {
    fun observarProductos(): Flow<List<ProductoEntity>> = catalogoDao.observarProductos()
    fun observarUnidades(): Flow<List<UnidadEntity>> = catalogoDao.observarUnidades()
    fun observarImpuestos(): Flow<List<ImpuestoEntity>> = catalogoDao.observarImpuestos()

    suspend fun obtenerProducto(id: String) = catalogoDao.obtenerProducto(id)
    suspend fun obtenerUnidad(id: String) = catalogoDao.obtenerUnidad(id)
    suspend fun obtenerImpuesto(id: String) = catalogoDao.obtenerImpuesto(id)

    suspend fun guardarProducto(producto: ProductoEntity): Result<Unit> =
        llamarApi { api.guardarProducto(producto.id, producto) }.map { catalogoDao.guardarProducto(it) }

    suspend fun eliminarProducto(id: String): Result<Unit> =
        llamarApi { api.eliminarProducto(id) }.map { catalogoDao.eliminarProducto(id) }

    suspend fun guardarUnidad(unidad: UnidadEntity): Result<Unit> =
        llamarApi { api.guardarUnidad(unidad.id, unidad) }.map { catalogoDao.guardarUnidad(it) }

    suspend fun eliminarUnidad(id: String): Result<Unit> =
        llamarApi { api.eliminarUnidad(id) }.map { catalogoDao.eliminarUnidad(id) }

    suspend fun guardarImpuesto(impuesto: ImpuestoEntity): Result<Unit> =
        llamarApi { api.guardarImpuesto(impuesto.id, impuesto) }.map { catalogoDao.guardarImpuesto(it) }

    suspend fun eliminarImpuesto(id: String): Result<Unit> =
        llamarApi { api.eliminarImpuesto(id) }.map { catalogoDao.eliminarImpuesto(id) }
}
