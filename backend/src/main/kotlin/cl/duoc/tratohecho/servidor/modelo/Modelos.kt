package cl.duoc.tratohecho.servidor.modelo

// Modelos que viajan entre la app y el servidor como JSON.
// Son los mismos de la app (data/local/entity y domain/model) sin las anotaciones de Room:
// si se cambia un campo en la app, hay que cambiarlo también aquí.

// ---------------- Enumeraciones ----------------

enum class Rol(val etiqueta: String) {
    ADMINISTRADOR("Administrador"),
    SUPERVISOR("Supervisor"),
    VENDEDOR("Vendedor")
}

enum class EstadoCotizacion(val etiqueta: String) {
    BORRADOR("Borrador"),
    ENVIADA("Enviada"),
    ACEPTADA("Aceptada"),
    RECHAZADA("Rechazada"),
    FACTURADA("Facturada"),
    VENCIDA("Vencida"),
    ANULADA("Anulada"),
    REEMPLAZADA("Reemplazada")
}

enum class Moneda(val etiqueta: String, val simbolo: String, val decimales: Int) {
    CLP("Peso chileno", "$", 0),
    USD("Dólar", "US$", 2),
    UF("Unidad de Fomento", "UF", 2)
}

enum class TipoDescuento(val etiqueta: String) {
    PORCENTAJE("%"),
    MONTO("Monto")
}

data class TotalesCotizacion(
    val subtotal: Double = 0.0,
    val descuentoGlobal: Double = 0.0,
    val neto: Double = 0.0,
    val impuestos: Double = 0.0,
    val total: Double = 0.0
)

// ---------------- Entidades ----------------

data class EmpresaEntity(
    val id: String,
    val razonSocial: String,
    val rut: String,
    val giro: String,
    val direccion: String,
    val correo: String,
    val telefono: String
)

data class UsuarioEntity(
    val id: String,
    val empresaId: String,
    val nombre: String,
    val correo: String,
    val rol: Rol,
    val activo: Boolean
)

data class UnidadEntity(
    val id: String,
    val empresaId: String,
    val nombre: String,
    val abreviatura: String
)

data class ImpuestoEntity(
    val id: String,
    val empresaId: String,
    val nombre: String,
    val porcentaje: Double
)

data class ProductoEntity(
    val id: String,
    val empresaId: String,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val unidadId: String,
    val impuestoId: String
)

data class ClienteEntity(
    val id: String,
    val empresaId: String,
    val vendedorId: String,
    val rut: String,
    val razonSocial: String,
    val giro: String,
    val direccion: String,
    val correo: String,
    val telefono: String,
    val vendedorNombre: String = "",
    val activo: Boolean = true,
    val pendienteSync: Boolean = false
)

data class ContactoEntity(
    val id: String,
    val clienteId: String,
    val nombre: String,
    val cargo: String,
    val correo: String,
    val telefono: String
)

data class ClienteConContactos(
    val cliente: ClienteEntity,
    val contactos: List<ContactoEntity>
)

data class CotizacionEntity(
    val id: String,
    val empresaId: String,
    val numero: String,
    val version: Int = 1,
    val cotizacionOrigenId: String? = null,
    val clienteId: String,
    val clienteNombre: String,
    val contactoId: String? = null,
    val vendedorId: String,
    val vendedorNombre: String,
    val moneda: Moneda = Moneda.CLP,
    val tipoCambio: Double = 1.0,
    val fechaTipoCambio: String? = null,
    val diasValidez: Int = 15,
    val descuentoGlobalTipo: TipoDescuento = TipoDescuento.PORCENTAJE,
    val descuentoGlobalValor: Double = 0.0,
    val notas: String = "",
    val estado: EstadoCotizacion = EstadoCotizacion.BORRADOR,
    val total: Double = 0.0,
    val fechaCreacion: Long,
    val fechaEnvio: Long? = null,
    val fechaVencimiento: Long? = null,
    val fechaRespuesta: Long? = null,
    val fechaFacturacion: Long? = null,
    val pendienteSync: Boolean = false,
    val envioPendiente: Boolean = false
)

data class ItemCotizacionEntity(
    val id: String,
    val cotizacionId: String,
    val productoId: String?,
    val descripcion: String,
    val unidad: String,
    val cantidad: Double,
    val precioUnitario: Double,
    val descuentoTipo: TipoDescuento = TipoDescuento.PORCENTAJE,
    val descuentoValor: Double = 0.0,
    val impuestoNombre: String,
    val impuestoPorcentaje: Double,
    val orden: Int = 0
)

data class CotizacionConItems(
    val cotizacion: CotizacionEntity,
    val items: List<ItemCotizacionEntity>
)

// ---------------- Peticiones y respuestas ----------------

data class LoginRequest(val correo: String, val clave: String)

data class LoginResponse(val token: String, val usuario: UsuarioEntity, val empresa: EmpresaEntity)

data class UsuarioRequest(val usuario: UsuarioEntity, val clave: String?)

data class ReasignarClienteRequest(val vendedorId: String)

data class ErrorResponse(val mensaje: String)
