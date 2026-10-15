package cl.duoc.tratohecho.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import cl.duoc.tratohecho.data.local.dao.CatalogoDao
import cl.duoc.tratohecho.data.local.dao.ClienteDao
import cl.duoc.tratohecho.data.local.dao.CotizacionDao
import cl.duoc.tratohecho.data.local.dao.EmpresaDao
import cl.duoc.tratohecho.data.local.dao.UsuarioDao
import cl.duoc.tratohecho.data.local.entity.ClienteEntity
import cl.duoc.tratohecho.data.local.entity.ContactoEntity
import cl.duoc.tratohecho.data.local.entity.CotizacionEntity
import cl.duoc.tratohecho.data.local.entity.EmpresaEntity
import cl.duoc.tratohecho.data.local.entity.ImpuestoEntity
import cl.duoc.tratohecho.data.local.entity.ItemCotizacionEntity
import cl.duoc.tratohecho.data.local.entity.ProductoEntity
import cl.duoc.tratohecho.data.local.entity.UnidadEntity
import cl.duoc.tratohecho.data.local.entity.UsuarioEntity

// Base de datos local (SQLite con Room). Permite trabajar sin conexión.
@Database(
    entities = [
        EmpresaEntity::class,
        UsuarioEntity::class,
        ClienteEntity::class,
        ContactoEntity::class,
        UnidadEntity::class,
        ImpuestoEntity::class,
        ProductoEntity::class,
        CotizacionEntity::class,
        ItemCotizacionEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun empresaDao(): EmpresaDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun clienteDao(): ClienteDao
    abstract fun catalogoDao(): CatalogoDao
    abstract fun cotizacionDao(): CotizacionDao
}
