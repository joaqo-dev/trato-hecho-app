package cl.duoc.tratohecho.domain.model

// Perfiles de usuario dentro de una empresa cliente (tenant)
enum class Rol(val etiqueta: String) {
    ADMINISTRADOR("Administrador"),
    SUPERVISOR("Supervisor"),
    VENDEDOR("Vendedor")
}
