package cl.duoc.tratohecho.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Catálogo de tipos de unidad (hora, m², unidad, etc.)
@Entity(tableName = "unidades")
data class UnidadEntity(
    @PrimaryKey val id: String,
    val empresaId: String,
    val nombre: String,
    val abreviatura: String
)
