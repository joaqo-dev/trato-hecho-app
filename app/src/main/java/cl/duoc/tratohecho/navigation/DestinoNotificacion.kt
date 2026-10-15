package cl.duoc.tratohecho.navigation

// Cotización que se debe abrir al tocar una notificación
data class DestinoNotificacion(
    val cotizacionId: String,
    val abrirCorreo: Boolean
)
