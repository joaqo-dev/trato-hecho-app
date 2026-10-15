package cl.duoc.tratohecho.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import cl.duoc.tratohecho.domain.model.Rol
import cl.duoc.tratohecho.domain.model.Sesion
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sesionDataStore: DataStore<Preferences> by preferencesDataStore(name = "sesion")

// Guarda la sesión del usuario para no pedir login cada vez que se abre la app
@Singleton
class SesionDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Claves {
        val TOKEN = stringPreferencesKey("token")
        val USUARIO_ID = stringPreferencesKey("usuario_id")
        val NOMBRE = stringPreferencesKey("nombre")
        val CORREO = stringPreferencesKey("correo")
        val ROL = stringPreferencesKey("rol")
        val EMPRESA_ID = stringPreferencesKey("empresa_id")
        val EMPRESA_NOMBRE = stringPreferencesKey("empresa_nombre")
    }

    val sesion: Flow<Sesion?> = context.sesionDataStore.data.map { prefs ->
        val token = prefs[Claves.TOKEN] ?: return@map null
        Sesion(
            token = token,
            usuarioId = prefs[Claves.USUARIO_ID] ?: "",
            nombre = prefs[Claves.NOMBRE] ?: "",
            correo = prefs[Claves.CORREO] ?: "",
            rol = Rol.valueOf(prefs[Claves.ROL] ?: Rol.VENDEDOR.name),
            empresaId = prefs[Claves.EMPRESA_ID] ?: "",
            empresaNombre = prefs[Claves.EMPRESA_NOMBRE] ?: ""
        )
    }

    suspend fun obtenerSesion(): Sesion? = sesion.first()

    suspend fun guardar(sesion: Sesion) {
        context.sesionDataStore.edit { prefs ->
            prefs[Claves.TOKEN] = sesion.token
            prefs[Claves.USUARIO_ID] = sesion.usuarioId
            prefs[Claves.NOMBRE] = sesion.nombre
            prefs[Claves.CORREO] = sesion.correo
            prefs[Claves.ROL] = sesion.rol.name
            prefs[Claves.EMPRESA_ID] = sesion.empresaId
            prefs[Claves.EMPRESA_NOMBRE] = sesion.empresaNombre
        }
    }

    suspend fun cerrar() {
        context.sesionDataStore.edit { it.clear() }
    }
}
