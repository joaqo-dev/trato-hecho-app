package cl.duoc.tratohecho.ui.screens.cotizaciones

import androidx.compose.material.icons.filled.CloudOff
import cl.duoc.tratohecho.ui.components.CargandoDatos
import cl.duoc.tratohecho.ui.components.ContenidoActualizable
import cl.duoc.tratohecho.ui.components.ESPACIO_BARRA_NAVEGACION
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.tratohecho.domain.model.EstadoCotizacion
import cl.duoc.tratohecho.domain.model.Rol
import cl.duoc.tratohecho.domain.model.Sesion
import cl.duoc.tratohecho.ui.components.BarraSeccion
import cl.duoc.tratohecho.ui.components.BotonFlotante
import cl.duoc.tratohecho.ui.components.CampoBusqueda
import cl.duoc.tratohecho.ui.components.EstadoVacio
import cl.duoc.tratohecho.ui.components.TarjetaCotizacion
import cl.duoc.tratohecho.ui.theme.Pildora
import cl.duoc.tratohecho.viewmodel.CotizacionesViewModel

@Composable
fun CotizacionesScreen(
    sesion: Sesion,
    onAbrir: (String) -> Unit,
    onNueva: () -> Unit,
    actualizando: Boolean,
    sinConexion: Boolean,
    onActualizar: () -> Unit,
    viewModel: CotizacionesViewModel = hiltViewModel()
) {
    val cotizaciones by viewModel.cotizaciones.collectAsStateWithLifecycle()
    val filtro by viewModel.filtro.collectAsStateWithLifecycle()
    val busqueda by viewModel.busqueda.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BarraSeccion(if (sesion.rol == Rol.VENDEDOR) "Mis cotizaciones" else "Cotizaciones") },
        floatingActionButton = {
            // Solo el vendedor arma cotizaciones nuevas
            if (sesion.rol == Rol.VENDEDOR) {
                BotonFlotante("Cotizar", Icons.Filled.Add, onNueva, Modifier.padding(bottom = ESPACIO_BARRA_NAVEGACION))
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            CampoBusqueda(
                valor = busqueda,
                onValorChange = viewModel::onBusquedaChange,
                placeholder = "Buscar por número o cliente",
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            // Filtros por estado
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                EstadoCotizacion.entries.forEach { estado ->
                    FilterChip(
                        selected = filtro == estado,
                        onClick = { viewModel.onFiltroChange(estado) },
                        label = { Text(estado.etiqueta) },
                        shape = Pildora,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
            val lista = cotizaciones
            // Deslizar hacia abajo vuelve a pedir los datos a la API
            ContenidoActualizable(actualizando, onActualizar, Modifier.fillMaxSize()) {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = ESPACIO_BARRA_NAVEGACION + 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    when {
                        lista == null || (lista.isEmpty() && actualizando) -> item { CargandoDatos("Cargando cotizaciones…") }
                        lista.isEmpty() && sinConexion && busqueda.isBlank() && filtro == null -> item {
                            EstadoVacio(
                                icono = Icons.Filled.CloudOff,
                                titulo = "Sin conexión",
                                mensaje = "No hay cotizaciones guardadas en el teléfono. Conéctate y desliza hacia abajo para descargarlas."
                            )
                        }
                        lista.isEmpty() -> item {
                            EstadoVacio(
                                icono = Icons.Filled.Description,
                                titulo = "No hay cotizaciones",
                                mensaje = when {
                                    busqueda.isNotBlank() -> "Ninguna coincide con \"$busqueda\"."
                                    filtro != null -> "No hay cotizaciones en estado ${filtro?.etiqueta}."
                                    else -> "Crea una cotización y envíala en la misma visita."
                                }
                            )
                        }
                        else -> items(lista, key = { it.id }) { cot ->
                            TarjetaCotizacion(
                                cotizacion = cot,
                                onClick = { onAbrir(cot.id) },
                                mostrarVendedor = sesion.rol != Rol.VENDEDOR,
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }
}
