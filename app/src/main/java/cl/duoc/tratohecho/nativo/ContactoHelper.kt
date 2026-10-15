package cl.duoc.tratohecho.nativo

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

// Recurso nativo: teléfono y correo. Abre el marcador (no requiere permiso de llamadas)
// o la app de correo con el destinatario listo.
object ContactoHelper {

    fun llamar(context: Context, telefono: String): Boolean = abrir(
        context,
        Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + telefono.replace(" ", "")))
    )

    fun escribirCorreo(context: Context, correo: String): Boolean = abrir(
        context,
        Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$correo"))
    )

    private fun abrir(context: Context, intent: Intent): Boolean = try {
        context.startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}
