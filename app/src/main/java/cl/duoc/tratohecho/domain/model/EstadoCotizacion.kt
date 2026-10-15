package cl.duoc.tratohecho.domain.model

// Ciclo de vida de una cotización
enum class EstadoCotizacion(val etiqueta: String) {
    BORRADOR("Borrador"),
    ENVIADA("Enviada"),
    ACEPTADA("Aceptada"),
    RECHAZADA("Rechazada"),
    FACTURADA("Facturada"),
    VENCIDA("Vencida"),
    ANULADA("Anulada"),
    REEMPLAZADA("Reemplazada");

    // Solo un borrador se puede modificar; lo demás requiere una nueva versión
    val esEditable: Boolean get() = this == BORRADOR

    val permiteNuevaVersion: Boolean get() = this == ENVIADA || this == RECHAZADA || this == VENCIDA

    val permiteAnular: Boolean get() = this == BORRADOR || this == ENVIADA || this == VENCIDA
}
