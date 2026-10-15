package cl.duoc.tratohecho.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class CotizacionConItems(
    @Embedded val cotizacion: CotizacionEntity,
    @Relation(parentColumn = "id", entityColumn = "cotizacionId")
    val items: List<ItemCotizacionEntity>
)
