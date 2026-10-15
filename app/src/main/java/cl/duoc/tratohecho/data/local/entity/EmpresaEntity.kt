package cl.duoc.tratohecho.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Datos con que la empresa (tenant) se identifica como emisor en sus cotizaciones
@Entity(tableName = "empresa")
data class EmpresaEntity(
    @PrimaryKey val id: String,
    val razonSocial: String,
    val rut: String,
    val giro: String,
    val direccion: String,
    val correo: String,
    val telefono: String
)
