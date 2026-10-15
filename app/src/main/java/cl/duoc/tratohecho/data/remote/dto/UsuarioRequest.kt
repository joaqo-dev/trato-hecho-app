package cl.duoc.tratohecho.data.remote.dto

import cl.duoc.tratohecho.data.local.entity.UsuarioEntity

// La clave solo viaja a la API (nunca se guarda en el teléfono). Al editar puede ir vacía.
data class UsuarioRequest(
    val usuario: UsuarioEntity,
    val clave: String?
)
