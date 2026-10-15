package cl.duoc.tratohecho.data.remote.dto

import cl.duoc.tratohecho.data.local.entity.EmpresaEntity
import cl.duoc.tratohecho.data.local.entity.UsuarioEntity

data class LoginRequest(
    val correo: String,
    val clave: String
)

data class LoginResponse(
    val token: String,
    val usuario: UsuarioEntity,
    val empresa: EmpresaEntity
)
