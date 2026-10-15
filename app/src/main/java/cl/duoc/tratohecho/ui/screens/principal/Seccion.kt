package cl.duoc.tratohecho.ui.screens.principal

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import cl.duoc.tratohecho.domain.model.Rol
import cl.duoc.tratohecho.navigation.AppRoutes

// Destinos de la barra de navegación inferior
data class Seccion(val ruta: String, val titulo: String, val icono: ImageVector)

private val INICIO = Seccion(AppRoutes.INICIO, "Inicio", Icons.Filled.Dashboard)
private val COTIZACIONES = Seccion(AppRoutes.COTIZACIONES, "Cotizaciones", Icons.Filled.Description)
private val CLIENTES = Seccion(AppRoutes.CLIENTES, "Clientes", Icons.Filled.People)
private val CATALOGO = Seccion(AppRoutes.CATALOGO, "Catálogo", Icons.Filled.Inventory2)
private val AJUSTES = Seccion(AppRoutes.AJUSTES, "Ajustes", Icons.Filled.Settings)

// Cada perfil ve un menú distinto
fun seccionesPara(rol: Rol): List<Seccion> = when (rol) {
    Rol.ADMINISTRADOR -> listOf(INICIO, COTIZACIONES, CLIENTES, CATALOGO, AJUSTES)
    Rol.SUPERVISOR, Rol.VENDEDOR -> listOf(INICIO, COTIZACIONES, CLIENTES, CATALOGO)
}
