package cl.duoc.tratohecho.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clientes")
data class ClienteEntity(
    @PrimaryKey val id: String,
    val empresaId: String,
    val vendedorId: String,
    val rut: String,
    val razonSocial: String,
    val giro: String,
    val direccion: String,
    val correo: String,
    val telefono: String,
    // Vendedor a cargo del cliente (el administrador o supervisor puede reasignarlo)
    val vendedorNombre: String = "",
    // Baja lógica: el cliente nunca se borra; dado de baja ya no se puede cotizar, pero su historial se conserva
    val activo: Boolean = true,
    // true = creado o editado sin conexión, falta subirlo a la API
    val pendienteSync: Boolean = false
)
