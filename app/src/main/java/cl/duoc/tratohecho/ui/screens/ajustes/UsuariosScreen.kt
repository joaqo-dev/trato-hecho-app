package cl.duoc.tratohecho.ui.screens.ajustes

import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import cl.duoc.tratohecho.ui.theme.Pildora
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import cl.duoc.tratohecho.ui.components.AccionMenu
import cl.duoc.tratohecho.ui.components.MenuAcciones
import cl.duoc.tratohecho.ui.components.BotonFlotante
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.tratohecho.data.local.entity.UsuarioEntity
import cl.duoc.tratohecho.domain.model.Sesion
import cl.duoc.tratohecho.ui.components.BarraConVolver
import cl.duoc.tratohecho.ui.components.DialogoConfirmacion
import cl.duoc.tratohecho.viewmodel.UsuariosViewModel

@Composable
fun UsuariosScreen(
    sesion: Sesion,
    mostrarMensaje: (String) -> Unit,
    onVolver: () -> Unit,
    onNuevo: () -> Unit,
    onEditar: (String) -> Unit,
    viewModel: UsuariosViewModel = hiltViewModel()
) {
    val usuarios by viewModel.usuarios.collectAsStateWithLifecycle()
    val cargando by viewModel.cargando.collectAsStateWithLifecycle()
    var aEliminar by remember { mutableStateOf<UsuarioEntity?>(null) }

    LaunchedEffect(Unit) { viewModel.mensajes.collect { mostrarMensaje(it) } }

    Scaffold(
        topBar = { BarraConVolver("Usuarios", onVolver) },
        floatingActionButton = {
            BotonFlotante("Nuevo usuario", Icons.Filled.PersonAdd, onNuevo)
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            AnimatedVisibility(visible = cargando) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(usuarios, key = { it.id }) { usuario ->
                    Card(
                        onClick = { onEditar(usuario.id) },
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp)
                                .alpha(if (usuario.activo) 1f else 0.5f)
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    usuario.nombre + if (usuario.id == sesion.usuarioId) " (tú)" else "",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(usuario.correo, style = MaterialTheme.typography.bodySmall)
                                Row(Modifier.padding(top = 6.dp)) {
                                    Text(
                                        usuario.rol.etiqueta,
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier
                                            .clip(Pildora)
                                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                            .padding(horizontal = 12.dp, vertical = 5.dp)
                                    )
                                    if (!usuario.activo) {
                                        Text(
                                            "  Desactivado",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.align(Alignment.CenterVertically)
                                        )
                                    }
                                }
                            }
                            MenuAcciones(
                                buildList {
                                    add(AccionMenu("Editar usuario", Icons.Outlined.Edit) { onEditar(usuario.id) })
                                    // No se puede eliminar el propio usuario
                                    if (usuario.id != sesion.usuarioId) {
                                        add(AccionMenu("Eliminar usuario", Icons.Outlined.Delete, destructiva = true) { aEliminar = usuario })
                                    }
                                },
                                descripcion = "Opciones de ${usuario.nombre}"
                            )
                        }
                    }
                }
            }
        }
    }

    aEliminar?.let { usuario ->
        DialogoConfirmacion(
            titulo = "Eliminar usuario",
            mensaje = "¿Eliminar a ${usuario.nombre}? Si tiene clientes o cotizaciones, mejor desactívalo.",
            textoConfirmar = "Eliminar",
            onConfirmar = {
                viewModel.eliminar(usuario.id)
                aEliminar = null
            },
            onCancelar = { aEliminar = null }
        )
    }
}
