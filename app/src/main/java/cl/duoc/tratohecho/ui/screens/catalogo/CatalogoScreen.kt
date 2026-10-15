package cl.duoc.tratohecho.ui.screens.catalogo

import cl.duoc.tratohecho.ui.components.ESPACIO_BARRA_NAVEGACION
import cl.duoc.tratohecho.ui.theme.Pildora
import cl.duoc.tratohecho.ui.components.BarraSeccion
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.tratohecho.domain.model.Rol
import cl.duoc.tratohecho.domain.model.Sesion
import cl.duoc.tratohecho.ui.components.DialogoConfirmacion
import cl.duoc.tratohecho.ui.components.EstadoVacio
import cl.duoc.tratohecho.utils.Formatos
import cl.duoc.tratohecho.viewmodel.CatalogoViewModel

private data class ElementoAEliminar(val tipo: Int, val id: String, val nombre: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    sesion: Sesion,
    mostrarMensaje: (String) -> Unit,
    onNuevoProducto: () -> Unit,
    onEditarProducto: (String) -> Unit,
    onNuevaUnidad: () -> Unit,
    onEditarUnidad: (String) -> Unit,
    onNuevoImpuesto: () -> Unit,
    onEditarImpuesto: (String) -> Unit,
    viewModel: CatalogoViewModel = hiltViewModel()
) {
    val productos by viewModel.productos.collectAsStateWithLifecycle()
    val unidades by viewModel.unidades.collectAsStateWithLifecycle()
    val impuestos by viewModel.impuestos.collectAsStateWithLifecycle()
    var pestana by rememberSaveable { mutableIntStateOf(0) }
    var aEliminar by remember { mutableStateOf<ElementoAEliminar?>(null) }
    // Solo el administrador parametriza el catálogo; el resto lo consulta
    val esAdmin = sesion.rol == Rol.ADMINISTRADOR

    LaunchedEffect(Unit) { viewModel.mensajes.collect { mostrarMensaje(it) } }

    Scaffold(
        topBar = { BarraSeccion("Catálogo") },
        floatingActionButton = {
            if (esAdmin) {
                FloatingActionButton(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = Pildora,
                    modifier = Modifier.padding(bottom = ESPACIO_BARRA_NAVEGACION),
                    onClick = {
                    when (pestana) {
                        0 -> onNuevoProducto()
                        1 -> onNuevaUnidad()
                        else -> onNuevoImpuesto()
                    }
                    }
                ) { Icon(Icons.Filled.Add, contentDescription = "Agregar") }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            PrimaryTabRow(selectedTabIndex = pestana) {
                Tab(selected = pestana == 0, onClick = { pestana = 0 }, text = { Text("Productos") })
                Tab(selected = pestana == 1, onClick = { pestana = 1 }, text = { Text("Unidades") })
                Tab(selected = pestana == 2, onClick = { pestana = 2 }, text = { Text("Impuestos") })
            }
            if (!esAdmin) {
                Text(
                    "Solo lectura: el administrador mantiene el catálogo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 20.dp, top = 10.dp)
                )
            }
            // Transición animada al cambiar de pestaña
            AnimatedContent(targetState = pestana, label = "pestana_catalogo") { seleccion ->
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = ESPACIO_BARRA_NAVEGACION + 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (seleccion) {
                        0 -> {
                            if (productos.isEmpty()) item {
                                EstadoVacio(Icons.Filled.Inventory2, "Sin productos", "Agrega productos o servicios para cotizar más rápido.")
                            }
                            items(productos, key = { it.producto.id }) { p ->
                                FilaCatalogo(
                                    titulo = p.producto.nombre,
                                    subtitulo = "${p.unidad} · ${p.impuesto}" +
                                        if (p.producto.descripcion.isNotBlank()) " · ${p.producto.descripcion}" else "",
                                    valor = Formatos.monto(p.producto.precio),
                                    esAdmin = esAdmin,
                                    onClick = { onEditarProducto(p.producto.id) },
                                    onEliminar = { aEliminar = ElementoAEliminar(0, p.producto.id, p.producto.nombre) },
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }
                        1 -> {
                            if (unidades.isEmpty()) item {
                                EstadoVacio(Icons.Filled.Straighten, "Sin unidades", "Agrega tipos de unidad como hora o m².")
                            }
                            items(unidades, key = { it.id }) { u ->
                                FilaCatalogo(
                                    titulo = u.nombre,
                                    subtitulo = "Abreviatura: ${u.abreviatura}",
                                    valor = null,
                                    esAdmin = esAdmin,
                                    onClick = { onEditarUnidad(u.id) },
                                    onEliminar = { aEliminar = ElementoAEliminar(1, u.id, u.nombre) },
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }
                        else -> {
                            if (impuestos.isEmpty()) item {
                                EstadoVacio(Icons.Filled.Percent, "Sin impuestos", "Agrega impuestos como IVA 19% o Exento.")
                            }
                            items(impuestos, key = { it.id }) { i ->
                                FilaCatalogo(
                                    titulo = i.nombre,
                                    subtitulo = "Se aplica por ítem",
                                    valor = "${Formatos.numero(i.porcentaje)}%",
                                    esAdmin = esAdmin,
                                    onClick = { onEditarImpuesto(i.id) },
                                    onEliminar = { aEliminar = ElementoAEliminar(2, i.id, i.nombre) },
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    aEliminar?.let { elemento ->
        DialogoConfirmacion(
            titulo = "Eliminar",
            mensaje = "¿Eliminar \"${elemento.nombre}\" del catálogo? Requiere conexión.",
            textoConfirmar = "Eliminar",
            onConfirmar = {
                when (elemento.tipo) {
                    0 -> viewModel.eliminarProducto(elemento.id)
                    1 -> viewModel.eliminarUnidad(elemento.id)
                    else -> viewModel.eliminarImpuesto(elemento.id)
                }
                aEliminar = null
            },
            onCancelar = { aEliminar = null }
        )
    }
}

@Composable
private fun FilaCatalogo(
    titulo: String,
    subtitulo: String,
    valor: String?,
    esAdmin: Boolean,
    onClick: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        enabled = esAdmin,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 18.dp, top = 14.dp, bottom = 14.dp, end = 6.dp)) {
            Column(Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (valor != null) {
                Text(
                    valor,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
            if (esAdmin) {
                IconButton(onClick = onEliminar) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
