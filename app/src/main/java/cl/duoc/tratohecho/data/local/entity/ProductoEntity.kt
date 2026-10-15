package cl.duoc.tratohecho.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Producto o servicio del catálogo. El precio está en pesos chilenos.
@Entity(tableName = "productos")
data class ProductoEntity(
    @PrimaryKey val id: String,
    val empresaId: String,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val unidadId: String,
    val impuestoId: String
)
