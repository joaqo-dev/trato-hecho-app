package cl.duoc.tratohecho.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import cl.duoc.tratohecho.domain.model.Rol

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey val id: String,
    val empresaId: String,
    val nombre: String,
    val correo: String,
    val rol: Rol,
    val activo: Boolean
)
