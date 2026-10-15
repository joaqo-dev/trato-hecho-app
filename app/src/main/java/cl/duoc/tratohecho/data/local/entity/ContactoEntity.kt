package cl.duoc.tratohecho.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contactos",
    foreignKeys = [ForeignKey(
        entity = ClienteEntity::class,
        parentColumns = ["id"],
        childColumns = ["clienteId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("clienteId")]
)
data class ContactoEntity(
    @PrimaryKey val id: String,
    val clienteId: String,
    val nombre: String,
    val cargo: String,
    val correo: String,
    val telefono: String
)
