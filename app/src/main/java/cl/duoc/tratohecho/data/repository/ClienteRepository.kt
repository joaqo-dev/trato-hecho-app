package cl.duoc.tratohecho.data.repository

import cl.duoc.tratohecho.data.local.dao.ClienteDao
import cl.duoc.tratohecho.data.local.dao.CotizacionDao
import cl.duoc.tratohecho.data.local.entity.ClienteConContactos
import cl.duoc.tratohecho.data.local.entity.ClienteEntity
import cl.duoc.tratohecho.data.local.entity.ContactoEntity
import cl.duoc.tratohecho.data.remote.TratoHechoApi
import cl.duoc.tratohecho.data.remote.llamarApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

// Los clientes se guardan primero en Room (funciona sin conexión) y luego se sincronizan con la API
@Singleton
class ClienteRepository @Inject constructor(
    private val api: TratoHechoApi,
    private val clienteDao: ClienteDao,
    private val cotizacionDao: CotizacionDao
) {
    fun observarClientes(): Flow<List<ClienteConContactos>> = clienteDao.observarClientes()

    fun observarCliente(id: String): Flow<ClienteConContactos?> = clienteDao.observarCliente(id)

    suspend fun obtenerCliente(id: String): ClienteConContactos? = clienteDao.obtenerCliente(id)

    suspend fun existeRut(rut: String, idExcluido: String): Boolean =
        clienteDao.contarPorRut(rut, idExcluido) > 0

    suspend fun guardar(cliente: ClienteEntity, contactos: List<ContactoEntity>) {
        clienteDao.guardarConContactos(cliente.copy(pendienteSync = true), contactos)
    }

    // Eliminar requiere conexión para confirmarlo con la API
    suspend fun eliminar(id: String): Result<Unit> {
        if (cotizacionDao.contarPorCliente(id) > 0) {
            return Result.failure(Exception("El cliente tiene cotizaciones y no se puede eliminar"))
        }
        return llamarApi { api.eliminarCliente(id) }.map { clienteDao.eliminar(id) }
    }
}
