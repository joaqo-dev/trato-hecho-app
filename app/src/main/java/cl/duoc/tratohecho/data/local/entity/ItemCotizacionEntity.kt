package cl.duoc.tratohecho.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import cl.duoc.tratohecho.domain.model.TipoDescuento

@Entity(
    tableName = "items_cotizacion",
    foreignKeys = [ForeignKey(
        entity = CotizacionEntity::class,
        parentColumns = ["id"],
        childColumns = ["cotizacionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("cotizacionId")]
)
data class ItemCotizacionEntity(
    @PrimaryKey val id: String,
    val cotizacionId: String,
    val productoId: String?,
    val descripcion: String,
    val unidad: String,
    val cantidad: Double,
    val precioUnitario: Double,         // en la moneda de la cotización
    val descuentoTipo: TipoDescuento = TipoDescuento.PORCENTAJE,
    val descuentoValor: Double = 0.0,
    val impuestoNombre: String,
    val impuestoPorcentaje: Double,
    val orden: Int = 0
)
