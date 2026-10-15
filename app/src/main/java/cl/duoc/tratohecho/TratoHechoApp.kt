package cl.duoc.tratohecho

import android.app.Application
import cl.duoc.tratohecho.data.sync.GestorSincronizacion
import cl.duoc.tratohecho.nativo.MonitorRed
import cl.duoc.tratohecho.nativo.NotificacionesHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TratoHechoApp : Application() {

    @Inject lateinit var monitorRed: MonitorRed
    @Inject lateinit var notificaciones: NotificacionesHelper
    @Inject lateinit var gestorSincronizacion: GestorSincronizacion

    override fun onCreate() {
        super.onCreate()
        notificaciones.crearCanal()
        // Se escucha la red mientras el proceso de la app esté vivo (también en segundo plano)
        monitorRed.iniciar()
        gestorSincronizacion.iniciar()
    }
}
