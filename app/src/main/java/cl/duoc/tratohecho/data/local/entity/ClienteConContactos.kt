package cl.duoc.tratohecho.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ClienteConContactos(
    @Embedded val cliente: ClienteEntity,
    @Relation(parentColumn = "id", entityColumn = "clienteId")
    val contactos: List<ContactoEntity>
)
