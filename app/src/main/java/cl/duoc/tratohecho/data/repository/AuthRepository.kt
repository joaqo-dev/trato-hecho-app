package cl.duoc.tratohecho.data.repository

import cl.duoc.tratohecho.data.local.AppDatabase
import cl.duoc.tratohecho.data.local.SesionDataStore
import cl.duoc.tratohecho.data.remote.TratoHechoApi
import cl.duoc.tratohecho.data.remote.dto.LoginRequest
import cl.duoc.tratohecho.data.remote.llamarApi
import cl.duoc.tratohecho.domain.model.Sesion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: TratoHechoApi,
    private val sesionDataStore: SesionDataStore,
    private val db: AppDatabase
) {
    val sesion: Flow<Sesion?> = sesionDataStore.sesion

    suspend fun login(correo: String, clave: String): Result<Sesion> =
        llamarApi { api.login(LoginRequest(correo.trim(), clave)) }.map { respuesta ->
            val sesion = Sesion(
                token = respuesta.token,
                usuarioId = respuesta.usuario.id,
                nombre = respuesta.usuario.nombre,
                correo = respuesta.usuario.correo,
                rol = respuesta.usuario.rol,
                empresaId = respuesta.empresa.id,
                empresaNombre = respuesta.empresa.razonSocial
            )
            // La base local solo guarda los datos de la empresa del usuario conectado
            withContext(Dispatchers.IO) { db.clearAllTables() }
            db.empresaDao().guardar(respuesta.empresa)
            sesionDataStore.guardar(sesion)
            sesion
        }

    suspend fun cerrarSesion() {
        sesionDataStore.cerrar()
        withContext(Dispatchers.IO) { db.clearAllTables() }
    }
}
