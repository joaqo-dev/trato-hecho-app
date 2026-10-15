package cl.duoc.tratohecho

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cl.duoc.tratohecho.navigation.AppNavigation
import cl.duoc.tratohecho.navigation.DestinoNotificacion
import cl.duoc.tratohecho.nativo.NotificacionesHelper
import cl.duoc.tratohecho.ui.theme.TratoHechoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Cotización a abrir cuando la app se abre desde una notificación
    private var destinoNotificacion by mutableStateOf<DestinoNotificacion?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        leerDestino(intent)
        setContent {
            TratoHechoTheme {
                AppNavigation(
                    destinoNotificacion = destinoNotificacion,
                    onDestinoAtendido = { destinoNotificacion = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        leerDestino(intent)
    }

    private fun leerDestino(intent: Intent?) {
        val id = intent?.getStringExtra(NotificacionesHelper.EXTRA_COTIZACION_ID) ?: return
        destinoNotificacion = DestinoNotificacion(
            cotizacionId = id,
            abrirCorreo = intent.getBooleanExtra(NotificacionesHelper.EXTRA_ABRIR_CORREO, false)
        )
        intent.removeExtra(NotificacionesHelper.EXTRA_COTIZACION_ID)
    }
}
