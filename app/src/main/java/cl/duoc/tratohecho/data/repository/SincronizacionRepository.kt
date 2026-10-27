package cl.duoc.tratohecho.data.repository

import cl.duoc.tratohecho.data.local.dao.CatalogoDao
import cl.duoc.tratohecho.data.local.dao.ClienteDao
import cl.duoc.tratohecho.data.local.dao.CotizacionDao
import cl.duoc.tratohecho.data.local.dao.EmpresaDao
import cl.duoc.tratohecho.data.local.dao.UsuarioDao
import cl.duoc.tratohecho.data.local.entity.CotizacionEntity
import cl.duoc.tratohecho.data.remote.TratoHechoApi
import cl.duoc.tratohecho.domain.model.EstadoCotizacion
import cl.duoc.tratohecho.domain.model.Rol
import cl.duoc.tratohecho.domain.model.Sesion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

// Resultado de una sincronización (lo usa GestorSincronizacion para mostrar notificaciones)
data class ResultadoSincronizacion(
    val listasParaEnviar: List<CotizacionEntity>,
    val respondidas: List<CotizacionEntity>,
    val facturadas: List<CotizacionEntity>
)

// Sube lo que se hizo sin conexión y baja lo último de la API
@Singleton
class SincronizacionRepository @Inject constructor(
    private val api: TratoHechoApi,
    private val empresaDao: EmpresaDao,
    private val usuarioDao: UsuarioDao,
    private val catalogoDao: CatalogoDao,
    private val clienteDao: ClienteDao,
    private val cotizacionDao: CotizacionDao
) {
    fun contarPendientes(): Flow<Int> =
        combine(clienteDao.contarPendientes(), cotizacionDao.contarPendientes()) { c, q -> c + q }

    suspend fun sincronizar(sesion: Sesion): ResultadoSincronizacion {
        val estadosAntes = cotizacionDao.obtenerTodas().associate { it.id to it.estado }
        val listasParaEnviar = mutableListOf<CotizacionEntity>()

        // 1) Subir clientes creados o editados sin conexión
        clienteDao.obtenerPendientes().forEach { cliente ->
            api.guardarCliente(cliente.cliente.id, cliente)
            clienteDao.marcarSincronizado(cliente.cliente.id)
        }

        // 2) Subir cotizaciones pendientes (las que estaban en cola se envían)
        cotizacionDao.obtenerPendientes().forEach { pendiente ->
            val cot = pendiente.cotizacion
            val aEnviar = cot.envioPendiente && cot.estado == EstadoCotizacion.BORRADOR
            val payload = if (aEnviar) pendiente.copy(cotizacion = cot.copy(estado = EstadoCotizacion.ENVIADA)) else pendiente
            val respuesta = api.guardarCotizacion(cot.id, payload)
            cotizacionDao.guardarConItems(
                respuesta.cotizacion.copy(pendienteSync = false, envioPendiente = false),
                respuesta.items
            )
            if (aEnviar && respuesta.cotizacion.estado == EstadoCotizacion.ENVIADA) {
                listasParaEnviar += respuesta.cotizacion
            }
        }

        // 3) Bajar los datos actualizados de la API
        empresaDao.guardar(api.obtenerEmpresa())
        catalogoDao.reemplazarCatalogo(api.obtenerUnidades(), api.obtenerImpuestos(), api.obtenerProductos())
        clienteDao.reemplazarSincronizados(api.obtenerClientes())
        val cotizaciones = api.obtenerCotizaciones()
        cotizacionDao.reemplazarSincronizadas(cotizaciones)
        if (sesion.rol == Rol.ADMINISTRADOR) {
            usuarioDao.reemplazarTodos(api.obtenerUsuarios())
        }

        // 4) Detectar cotizaciones que el cliente respondió en la página pública
        val respondidas = cotizaciones.map { it.cotizacion }.filter {
            estadosAntes[it.id] == EstadoCotizacion.ENVIADA &&
                (it.estado == EstadoCotizacion.ACEPTADA || it.estado == EstadoCotizacion.RECHAZADA)
        }
        // 5) Detectar cotizaciones que la empresa facturó en la plataforma
        val facturadas = cotizaciones.map { it.cotizacion }.filter {
            it.estado == EstadoCotizacion.FACTURADA &&
                estadosAntes[it.id].let { antes -> antes == EstadoCotizacion.ACEPTADA || antes == EstadoCotizacion.ENVIADA }
        }
        return ResultadoSincronizacion(listasParaEnviar, respondidas, facturadas)
    }
}
