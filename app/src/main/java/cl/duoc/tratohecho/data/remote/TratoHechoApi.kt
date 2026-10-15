package cl.duoc.tratohecho.data.remote

import cl.duoc.tratohecho.data.local.entity.ClienteConContactos
import cl.duoc.tratohecho.data.local.entity.CotizacionConItems
import cl.duoc.tratohecho.data.local.entity.EmpresaEntity
import cl.duoc.tratohecho.data.local.entity.ImpuestoEntity
import cl.duoc.tratohecho.data.local.entity.ProductoEntity
import cl.duoc.tratohecho.data.local.entity.UnidadEntity
import cl.duoc.tratohecho.data.local.entity.UsuarioEntity
import cl.duoc.tratohecho.data.remote.dto.LoginRequest
import cl.duoc.tratohecho.data.remote.dto.LoginResponse
import cl.duoc.tratohecho.data.remote.dto.UsuarioRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// API REST de la plataforma Trato Hecho (la misma que usa la web).
// En este proyecto la responde FakeApiInterceptor. La empresa se obtiene del token.
interface TratoHechoApi {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    // ---------- Empresa (emisor) ----------
    @GET("empresa")
    suspend fun obtenerEmpresa(): EmpresaEntity

    @PUT("empresa")
    suspend fun guardarEmpresa(@Body empresa: EmpresaEntity): EmpresaEntity

    // ---------- Usuarios ----------
    @GET("usuarios")
    suspend fun obtenerUsuarios(): List<UsuarioEntity>

    @PUT("usuarios/{id}")
    suspend fun guardarUsuario(@Path("id") id: String, @Body request: UsuarioRequest): UsuarioEntity

    @DELETE("usuarios/{id}")
    suspend fun eliminarUsuario(@Path("id") id: String)

    // ---------- Catálogo ----------
    @GET("unidades")
    suspend fun obtenerUnidades(): List<UnidadEntity>

    @PUT("unidades/{id}")
    suspend fun guardarUnidad(@Path("id") id: String, @Body unidad: UnidadEntity): UnidadEntity

    @DELETE("unidades/{id}")
    suspend fun eliminarUnidad(@Path("id") id: String)

    @GET("impuestos")
    suspend fun obtenerImpuestos(): List<ImpuestoEntity>

    @PUT("impuestos/{id}")
    suspend fun guardarImpuesto(@Path("id") id: String, @Body impuesto: ImpuestoEntity): ImpuestoEntity

    @DELETE("impuestos/{id}")
    suspend fun eliminarImpuesto(@Path("id") id: String)

    @GET("productos")
    suspend fun obtenerProductos(): List<ProductoEntity>

    @PUT("productos/{id}")
    suspend fun guardarProducto(@Path("id") id: String, @Body producto: ProductoEntity): ProductoEntity

    @DELETE("productos/{id}")
    suspend fun eliminarProducto(@Path("id") id: String)

    // ---------- Clientes ----------
    @GET("clientes")
    suspend fun obtenerClientes(): List<ClienteConContactos>

    @PUT("clientes/{id}")
    suspend fun guardarCliente(@Path("id") id: String, @Body cliente: ClienteConContactos): ClienteConContactos

    @DELETE("clientes/{id}")
    suspend fun eliminarCliente(@Path("id") id: String)

    // ---------- Cotizaciones ----------
    @GET("cotizaciones")
    suspend fun obtenerCotizaciones(): List<CotizacionConItems>

    @PUT("cotizaciones/{id}")
    suspend fun guardarCotizacion(@Path("id") id: String, @Body cotizacion: CotizacionConItems): CotizacionConItems

    // Solo para la demo: simula que el cliente respondió en la página pública
    @POST("cotizaciones/{id}/simular-respuesta")
    suspend fun simularRespuesta(@Path("id") id: String): CotizacionConItems
}
