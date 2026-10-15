package cl.duoc.tratohecho.ui.screens.principal

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Settings
import cl.duoc.tratohecho.domain.model.Rol
import cl.duoc.tratohecho.navigation.AppRoutes
import cl.duoc.tratohecho.ui.components.DestinoBarra

// Destinos de la barra de navegación inferior
private val INICIO = DestinoBarra(AppRoutes.INICIO, "Inicio", Icons.Rounded.Home)
private val COTIZACIONES = DestinoBarra(AppRoutes.COTIZACIONES, "Cotizaciones", Icons.Rounded.Description)
private val CLIENTES = DestinoBarra(AppRoutes.CLIENTES, "Clientes", Icons.Rounded.People)
private val CATALOGO = DestinoBarra(AppRoutes.CATALOGO, "Catálogo", Icons.Rounded.LocalOffer)
private val AJUSTES = DestinoBarra(AppRoutes.AJUSTES, "Ajustes", Icons.Rounded.Settings)

// Cada perfil ve un menú distinto
fun seccionesPara(rol: Rol): List<DestinoBarra> = when (rol) {
    Rol.ADMINISTRADOR -> listOf(INICIO, COTIZACIONES, CLIENTES, CATALOGO, AJUSTES)
    Rol.SUPERVISOR, Rol.VENDEDOR -> listOf(INICIO, COTIZACIONES, CLIENTES, CATALOGO)
}
