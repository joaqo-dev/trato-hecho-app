package cl.duoc.tratohecho.data.sync

import cl.duoc.tratohecho.data.local.SesionDataStore
import cl.duoc.tratohecho.data.remote.llamarApi
import cl.duoc.tratohecho.data.repository.IndicadoresEconomicosRepository
import cl.duoc.tratohecho.data.repository.ResultadoSincronizacion
import cl.duoc.tratohecho.data.repository.SincronizacionRepository
import cl.duoc.tratohecho.nativo.MonitorRed
import cl.duoc.tratohecho.nativo.NotificacionesHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

// Coordina la sincronización automática: cuando el MonitorRed avisa que volvió la señal,
// sube los clientes y cotizaciones pendientes y baja lo último de la API.
@Singleton
class GestorSincronizacion @Inject constructor(
    private val repository: SincronizacionRepository,
    private val monitorRed: MonitorRed,
    private val sesionDataStore: SesionDataStore,
    private val notificaciones: NotificacionesHelper,
    private val indicadoresRepository: IndicadoresEconomicosRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()

    private val _sincronizando = MutableStateFlow(false)
    val sincronizando: StateFlow<Boolean> = _sincronizando.asStateFlow()

    val pendientes: Flow<Int> = repository.contarPendientes()

    fun iniciar() {
        scope.launch {
            monitorRed.hayConexion.collect { conectado ->
                if (conectado) sincronizarAhora()
            }
        }
    }

    // notificarListas = false cuando la pantalla que envió la cotización abrirá el correo ella misma
    suspend fun sincronizarAhora(notificarListas: Boolean = true): Result<ResultadoSincronizacion> =
        mutex.withLock {
            val sesion = sesionDataStore.obtenerSesion()
                ?: return Result.failure(Exception("No hay sesión iniciada"))
            if (!monitorRed.estaConectado()) {
                return Result.failure(Exception("Sin conexión. Los cambios quedan guardados en el teléfono."))
            }
            _sincronizando.value = true
            val resultado = llamarApi { repository.sincronizar(sesion) }
            // También se guarda el dólar y la UF del día, para usarlos si después se pierde la señal
            indicadoresRepository.actualizar()
            _sincronizando.value = false

            resultado.onSuccess { r ->
                r.respondidas.forEach { notificaciones.notificarRespuesta(it) }
                if (notificarListas) r.listasParaEnviar.forEach { notificaciones.notificarListaParaEnviar(it) }
            }
            resultado
        }

    // Para llamarla desde lugares sin corrutina (ej: botón "sincronizar")
    fun sincronizarEnSegundoPlano() {
        scope.launch { sincronizarAhora() }
    }
}
