package cl.duoc.tratohecho.nativo

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import cl.duoc.tratohecho.MainActivity
import cl.duoc.tratohecho.R
import cl.duoc.tratohecho.data.local.entity.CotizacionEntity
import cl.duoc.tratohecho.domain.model.EstadoCotizacion
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

// Recurso nativo: notificaciones del sistema.
// Avisa cuando una cotización en cola quedó lista para enviar, cuando un cliente responde y cuando se factura.
@Singleton
class NotificacionesHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun crearCanal() {
        val canal = NotificationChannel(
            CANAL_ID,
            context.getString(R.string.canal_notificaciones_nombre),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.canal_notificaciones_descripcion)
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(canal)
    }

    fun tienePermiso(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    fun notificarListaParaEnviar(cotizacion: CotizacionEntity) {
        mostrar(
            cotizacion = cotizacion,
            titulo = "Cotización ${cotizacion.numeroCompleto} lista para enviar",
            texto = "Recuperaste la señal. Toca para enviarla por correo a ${cotizacion.clienteNombre}.",
            abrirCorreo = true
        )
    }

    fun notificarRespuesta(cotizacion: CotizacionEntity) {
        val aceptada = cotizacion.estado == EstadoCotizacion.ACEPTADA
        mostrar(
            cotizacion = cotizacion,
            titulo = if (aceptada) "¡Trato hecho! ${cotizacion.numeroCompleto} fue aceptada"
            else "${cotizacion.numeroCompleto} fue rechazada",
            texto = "${cotizacion.clienteNombre} respondió tu cotización.",
            abrirCorreo = false
        )
    }

    fun notificarFacturada(cotizacion: CotizacionEntity) {
        mostrar(
            cotizacion = cotizacion,
            titulo = "${cotizacion.numeroCompleto} fue facturada",
            texto = "Se registró la factura de la cotización de ${cotizacion.clienteNombre}.",
            abrirCorreo = false
        )
    }

    private fun mostrar(cotizacion: CotizacionEntity, titulo: String, texto: String, abrirCorreo: Boolean) {
        if (!tienePermiso()) return
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_COTIZACION_ID, cotizacion.id)
            putExtra(EXTRA_ABRIR_CORREO, abrirCorreo)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            cotizacion.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notificacion = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(cotizacion.id.hashCode(), notificacion)
        } catch (e: SecurityException) {
            // El usuario quitó el permiso de notificaciones
        }
    }

    companion object {
        const val CANAL_ID = "cotizaciones"
        const val EXTRA_COTIZACION_ID = "cotizacion_id"
        const val EXTRA_ABRIR_CORREO = "abrir_correo"
    }
}
