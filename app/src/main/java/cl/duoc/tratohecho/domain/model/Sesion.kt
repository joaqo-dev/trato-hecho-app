package cl.duoc.tratohecho.domain.model

// Datos del usuario que inició sesión (se guardan en DataStore)
data class Sesion(
    val token: String,
    val usuarioId: String,
    val nombre: String,
    val correo: String,
    val rol: Rol,
    val empresaId: String,
    val empresaNombre: String
)
