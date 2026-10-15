package cl.duoc.tratohecho.nativo

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

// Recurso nativo: estado de la red usando ConnectivityManager.
// Avisa cuando se pierde o se recupera la conexión (la app sigue escuchando aunque esté en segundo plano).
@Singleton
class MonitorRed @Inject constructor(
    @ApplicationContext context: Context
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _hayConexion = MutableStateFlow(revisarConexionActual())
    val hayConexion: StateFlow<Boolean> = _hayConexion.asStateFlow()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _hayConexion.value = true
        }

        override fun onLost(network: Network) {
            _hayConexion.value = revisarConexionActual()
        }

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            _hayConexion.value = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
    }

    fun iniciar() {
        connectivityManager.registerDefaultNetworkCallback(callback)
    }

    fun estaConectado(): Boolean = _hayConexion.value

    private fun revisarConexionActual(): Boolean {
        val red = connectivityManager.activeNetwork ?: return false
        val capacidades = connectivityManager.getNetworkCapabilities(red) ?: return false
        return capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
