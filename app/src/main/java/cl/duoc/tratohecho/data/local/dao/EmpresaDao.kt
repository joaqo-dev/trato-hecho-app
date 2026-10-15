package cl.duoc.tratohecho.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import cl.duoc.tratohecho.data.local.entity.EmpresaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmpresaDao {
    @Query("SELECT * FROM empresa LIMIT 1")
    fun observarEmpresa(): Flow<EmpresaEntity?>

    @Query("SELECT * FROM empresa LIMIT 1")
    suspend fun obtenerEmpresa(): EmpresaEntity?

    @Upsert
    suspend fun guardar(empresa: EmpresaEntity)
}
