package cl.duoc.tratohecho.nativo

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

// Recurso nativo: compartir. Prepara un Intent para abrir la app de correo del teléfono
// con el destinatario, el asunto, el mensaje y el PDF adjunto.
@Singleton
class CompartirHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun crearIntentCorreo(archivo: File, destinatario: String, asunto: String, mensaje: String): Intent {
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(destinatario))
            putExtra(Intent.EXTRA_SUBJECT, asunto)
            putExtra(Intent.EXTRA_TEXT, mensaje)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = ClipData.newRawUri("", uri)
        }
        // Apps de correo instaladas (las que abren un "mailto:")
        val appsCorreo = context.packageManager
            .queryIntentActivities(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")), 0)
            .map { it.activityInfo.packageName }
            .distinct()
        // Con una sola app de correo se abre directo; si hay varias o ninguna, se muestra el menú de compartir
        return if (appsCorreo.size == 1) {
            intent.setPackage(appsCorreo.first())
        } else {
            Intent.createChooser(intent, "Enviar cotización por correo")
        }
    }

    // Abre el PDF en el visor del sistema (desde ahí también se puede imprimir o compartir por otros medios)
    fun crearIntentVer(archivo: File): Intent {
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = ClipData.newRawUri("", uri)
        }
        return Intent.createChooser(intent, "Abrir cotización con")
    }
}
