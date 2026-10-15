package cl.duoc.tratohecho.data.repository

import cl.duoc.tratohecho.data.local.dao.UsuarioDao
import cl.duoc.tratohecho.data.local.entity.UsuarioEntity
import cl.duoc.tratohecho.data.remote.TratoHechoApi
import cl.duoc.tratohecho.data.remote.dto.UsuarioRequest
import cl.duoc.tratohecho.data.remote.llamarApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsuarioRepository @Inject constructor(
    private val api: TratoHechoApi,
    private val usuarioDao: UsuarioDao
) {
    fun observarUsuarios(): Flow<List<UsuarioEntity>> = usuarioDao.observarUsuarios()

    suspend fun obtenerUsuario(id: String): UsuarioEntity? = usuarioDao.obtenerPorId(id)

    suspend fun refrescar(): Result<Unit> =
        llamarApi { api.obtenerUsuarios() }.map { usuarioDao.reemplazarTodos(it) }

    suspend fun guardar(usuario: UsuarioEntity, clave: String?): Result<Unit> =
        llamarApi { api.guardarUsuario(usuario.id, UsuarioRequest(usuario, clave)) }.map { usuarioDao.guardar(it) }

    suspend fun eliminar(id: String): Result<Unit> =
        llamarApi { api.eliminarUsuario(id) }.map { usuarioDao.eliminar(id) }
}
