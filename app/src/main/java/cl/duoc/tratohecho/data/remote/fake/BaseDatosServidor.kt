package cl.duoc.tratohecho.data.remote.fake

import cl.duoc.tratohecho.data.local.entity.ClienteConContactos
import cl.duoc.tratohecho.data.local.entity.CotizacionConItems
import cl.duoc.tratohecho.data.local.entity.EmpresaEntity
import cl.duoc.tratohecho.data.local.entity.ImpuestoEntity
import cl.duoc.tratohecho.data.local.entity.ProductoEntity
import cl.duoc.tratohecho.data.local.entity.UnidadEntity
import cl.duoc.tratohecho.data.local.entity.UsuarioEntity

// "Base de datos" del servidor simulado. Contiene los datos de TODAS las empresas;
// el servidor filtra por empresa antes de responder.
data class BaseDatosServidor(
    val fechaSemilla: Long,
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
