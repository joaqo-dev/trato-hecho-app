package cl.duoc.tratohecho.servidor.datos

import cl.duoc.tratohecho.servidor.modelo.ClienteConContactos
import cl.duoc.tratohecho.servidor.modelo.CotizacionConItems
import cl.duoc.tratohecho.servidor.modelo.EmpresaEntity
import cl.duoc.tratohecho.servidor.modelo.ImpuestoEntity
import cl.duoc.tratohecho.servidor.modelo.ProductoEntity
import cl.duoc.tratohecho.servidor.modelo.UnidadEntity
import cl.duoc.tratohecho.servidor.modelo.UsuarioEntity

// Datos del servidor en memoria. Contiene TODAS las empresas; el servidor filtra por empresa
// antes de responder. AlmacenSqlite los guarda en la base de datos después de cada cambio.
data class BaseDatosServidor(
    val empresas: MutableList<EmpresaEntity> = mutableListOf(),
    val usuarios: MutableList<UsuarioServidor> = mutableListOf(),
    val unidades: MutableList<UnidadEntity> = mutableListOf(),
    val impuestos: MutableList<ImpuestoEntity> = mutableListOf(),
    val productos: MutableList<ProductoEntity> = mutableListOf(),
    val clientes: MutableList<ClienteConContactos> = mutableListOf(),
    val cotizaciones: MutableList<CotizacionConItems> = mutableListOf(),
    val correlativos: MutableMap<String, Int> = mutableMapOf()
)

data class UsuarioServidor(
    val usuario: UsuarioEntity,
    val clave: String
)
