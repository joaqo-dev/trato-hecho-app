package cl.duoc.tratohecho.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import cl.duoc.tratohecho.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios ORDER BY nombre")
    fun observarUsuarios(): Flow<List<UsuarioEntity>>

    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun obtenerPorId(id: String): UsuarioEntity?

    @Upsert
    suspend fun guardar(usuario: UsuarioEntity)

    @Query("DELETE FROM usuarios WHERE id = :id")
    suspend fun eliminar(id: String)

    @Query("DELETE FROM usuarios")
    suspend fun eliminarTodos()

    @Insert
    suspend fun insertarTodos(usuarios: List<UsuarioEntity>)

    @Transaction
    suspend fun reemplazarTodos(usuarios: List<UsuarioEntity>) {
        eliminarTodos()
        insertarTodos(usuarios)
    }
}
