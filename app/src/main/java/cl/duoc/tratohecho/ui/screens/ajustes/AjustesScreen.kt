package cl.duoc.tratohecho.ui.screens.ajustes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cl.duoc.tratohecho.domain.model.Sesion
import cl.duoc.tratohecho.ui.components.DialogoConfirmacion

// Sección exclusiva del administrador
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    sesion: Sesion,
    onUsuarios: () -> Unit,
    onEmpresa: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    var confirmarSalida by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text("Ajustes") }) }) { padding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                ListItem(
                    headlineContent = { Text(sesion.nombre) },
                    supportingContent = { Text("${sesion.correo}\n${sesion.rol.etiqueta} · ${sesion.empresaNombre}") },
                    leadingContent = { Icon(Icons.Filled.AccountCircle, contentDescription = null) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
            OpcionAjuste(Icons.Filled.Business, "Datos de la empresa", "RUT, razón social y datos que aparecen en el PDF", onEmpresa)
            OpcionAjuste(Icons.Filled.Group, "Usuarios", "Crear, editar y desactivar usuarios de la empresa", onUsuarios)
            OutlinedButton(onClick = { confirmarSalida = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Text("  Cerrar sesión")
            }
        }
    }

    if (confirmarSalida) {
        DialogoConfirmacion(
            titulo = "Cerrar sesión",
            mensaje = "¿Quieres cerrar tu sesión en este teléfono?",
            textoConfirmar = "Cerrar sesión",
            onConfirmar = {
                confirmarSalida = false
                onCerrarSesion()
            },
            onCancelar = { confirmarSalida = false }
        )
    }
}

@Composable
private fun OpcionAjuste(icono: ImageVector, titulo: String, descripcion: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        ListItem(
            headlineContent = { Text(titulo) },
            supportingContent = { Text(descripcion) },
            leadingContent = { Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}
