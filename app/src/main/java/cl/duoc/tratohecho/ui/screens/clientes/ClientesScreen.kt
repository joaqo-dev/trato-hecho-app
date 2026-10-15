package cl.duoc.tratohecho.ui.screens.clientes

import androidx.compose.ui.draw.alpha
import androidx.compose.material.icons.filled.CloudOff
import cl.duoc.tratohecho.ui.components.CargandoDatos
import cl.duoc.tratohecho.ui.components.ContenidoActualizable
import cl.duoc.tratohecho.ui.components.ESPACIO_BARRA_NAVEGACION
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.runtime.remember
import androidx.compose.foundation.interaction.MutableInteractionSource
import cl.duoc.tratohecho.ui.components.efectoPresion
import cl.duoc.tratohecho.ui.components.Avatar
import cl.duoc.tratohecho.ui.components.CampoBusqueda
import cl.duoc.tratohecho.ui.components.BotonFlotante
import cl.duoc.tratohecho.ui.components.BarraSeccion
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.tratohecho.data.local.entity.ClienteConContactos
import cl.duoc.tratohecho.domain.model.Rol
import cl.duoc.tratohecho.domain.model.Sesion
import cl.duoc.tratohecho.ui.components.EstadoVacio
import cl.duoc.tratohecho.viewmodel.ClientesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientesScreen(
    sesion: Sesion,
    onAbrir: (String) -> Unit,
    onNuevo: () -> Unit,
    actualizando: Boolean,
    sinConexion: Boolean,
    onActualizar: () -> Unit,
    viewModel: ClientesViewModel = hiltViewModel()
) {
    val clientes by viewModel.clientes.collectAsStateWithLifecycle()
    val busqueda by viewModel.busqueda.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { BarraSeccion(if (sesion.rol == Rol.VENDEDOR) "Mis clientes" else "Clientes") },
        floatingActionButton = {
            // Solo el vendedor registra clientes nuevos
            // Solo el administrador registra clientes
            if (sesion.rol == Rol.ADMINISTRADOR) {
                BotonFlotante("Nuevo cliente", Icons.Filled.PersonAdd, onNuevo, Modifier.padding(bottom = ESPACIO_BARRA_NAVEGACION))
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            CampoBusqueda(
                valor = busqueda,
                onValorChange = viewModel::onBusquedaChange,
                placeholder = "Buscar por nombre o RUT",
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
            )
            val lista = clientes
            ContenidoActualizable(actualizando, onActualizar, Modifier.fillMaxSize()) {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = ESPACIO_BARRA_NAVEGACION + 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    when {
                        lista == null || (lista.isEmpty() && actualizando) -> item { CargandoDatos("Cargando clientes…") }
                        lista.isEmpty() && sinConexion && busqueda.isBlank() -> item {
                            EstadoVacio(
                                icono = Icons.Filled.CloudOff,
                                titulo = "Sin conexión",
                                mensaje = "No hay clientes guardados en el teléfono. Conéctate y desliza hacia abajo para descargarlos."
                            )
                        }
                        lista.isEmpty() -> item {
                            EstadoVacio(
                                icono = Icons.Filled.PersonSearch,
                                titulo = if (busqueda.isBlank()) "Aún no hay clientes" else "Sin resultados",
                                mensaje = when {
                                    busqueda.isNotBlank() -> "Prueba con otro nombre o RUT."
                                    sesion.rol == Rol.ADMINISTRADOR -> "Registra el primer cliente y asígnale un vendedor, incluso sin conexión."
                                    else -> "El administrador aún no te asigna clientes."
                                }
                            )
                        }
                        else -> items(lista, key = { it.cliente.id }) { cliente ->
                            TarjetaCliente(
                                cliente,
                                mostrarVendedor = sesion.rol != Rol.VENDEDOR,
                                onClick = { onAbrir(cliente.cliente.id) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaCliente(
    cliente: ClienteConContactos,
    mostrarVendedor: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaccion = remember { MutableInteractionSource() }
    Card(
        onClick = onClick,
        interactionSource = interaccion,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
            .fillMaxWidth()
            .efectoPresion(interaccion)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(16.dp)
                .alpha(if (cliente.cliente.activo) 1f else 0.55f)
        ) {
            Avatar(cliente.cliente.razonSocial)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(cliente.cliente.razonSocial, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    if (mostrarVendedor) "${cliente.cliente.rut} · ${cliente.cliente.vendedorNombre}"
                    else "${cliente.cliente.rut} · ${cliente.contactos.size} contacto(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (!cliente.cliente.activo) {
                Text(
                    "De baja",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
            }
            if (cliente.cliente.pendienteSync) {
                Icon(Icons.Filled.CloudUpload, contentDescription = "Pendiente de sincronizar", tint = MaterialTheme.colorScheme.secondary)
            } else {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
