package cl.duoc.tratohecho.data.repository

import cl.duoc.tratohecho.data.local.dao.EmpresaDao
import cl.duoc.tratohecho.data.local.entity.EmpresaEntity
import cl.duoc.tratohecho.data.remote.TratoHechoApi
import cl.duoc.tratohecho.data.remote.llamarApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmpresaRepository @Inject constructor(
    private val api: TratoHechoApi,
    private val empresaDao: EmpresaDao
) {
    fun observarEmpresa(): Flow<EmpresaEntity?> = empresaDao.observarEmpresa()

    suspend fun obtenerEmpresa(): EmpresaEntity? = empresaDao.obtenerEmpresa()

    // Los datos del emisor se modifican directo en la API (requiere conexión)
    suspend fun guardar(empresa: EmpresaEntity): Result<Unit> =
        llamarApi { api.guardarEmpresa(empresa) }.map { empresaDao.guardar(it) }
}
