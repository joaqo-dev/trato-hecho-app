package cl.duoc.tratohecho.navigation

// Rutas centralizadas de la aplicación
object AppRoutes {
    // Navegación raíz
    const val LOGIN = "login"
    const val PRINCIPAL = "principal"

    // Secciones de la barra inferior (navegación anidada dentro de PRINCIPAL)
    const val INICIO = "inicio"
    const val COTIZACIONES = "cotizaciones"
    const val CLIENTES = "clientes"
    const val CATALOGO = "catalogo"
    const val AJUSTES = "ajustes"

    // Pantallas de detalle y formularios
    const val COTIZACION_DETALLE = "cotizacion/{id}?abrirCorreo={abrirCorreo}"
    const val COTIZACION_FORM = "cotizacion_form?id={id}"
    const val CLIENTE_DETALLE = "cliente/{id}"
    const val CLIENTE_FORM = "cliente_form?id={id}"
    const val PRODUCTO_FORM = "producto_form?id={id}"
    const val UNIDAD_FORM = "unidad_form?id={id}"
    const val IMPUESTO_FORM = "impuesto_form?id={id}"
    const val USUARIOS = "usuarios"
    const val USUARIO_FORM = "usuario_form?id={id}"
    const val EMPRESA_FORM = "empresa_form"

    fun cotizacionDetalle(id: String, abrirCorreo: Boolean = false) = "cotizacion/$id?abrirCorreo=$abrirCorreo"
    fun cotizacionForm(id: String? = null) = if (id == null) "cotizacion_form" else "cotizacion_form?id=$id"
    fun clienteDetalle(id: String) = "cliente/$id"
    fun clienteForm(id: String? = null) = if (id == null) "cliente_form" else "cliente_form?id=$id"
    fun productoForm(id: String? = null) = if (id == null) "producto_form" else "producto_form?id=$id"
    fun unidadForm(id: String? = null) = if (id == null) "unidad_form" else "unidad_form?id=$id"
    fun impuestoForm(id: String? = null) = if (id == null) "impuesto_form" else "impuesto_form?id=$id"
    fun usuarioForm(id: String? = null) = if (id == null) "usuario_form" else "usuario_form?id=$id"
}
