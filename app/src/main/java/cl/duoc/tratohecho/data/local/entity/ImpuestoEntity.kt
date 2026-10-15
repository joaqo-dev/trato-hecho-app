package cl.duoc.tratohecho.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Catálogo de impuestos (IVA 19%, Exento, etc.)
@Entity(tableName = "impuestos")
data class ImpuestoEntity(
    @PrimaryKey val id: String,
    val empresaId: String,
    val nombre: String,
    val porcentaje: Double
)
