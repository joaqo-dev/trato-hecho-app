package cl.duoc.tratohecho.ui.screens.cotizaciones

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import cl.duoc.tratohecho.ui.components.EstadoVacio
import cl.duoc.tratohecho.ui.components.TarjetaCotizacion
import cl.duoc.tratohecho.viewmodel.CotizacionesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CotizacionesScreen(
    sesion: Sesion,
    onAbrir: (String) -> Unit,
    onNueva: () -> Unit,
    viewModel: CotizacionesViewModel = hiltViewModel()
) {
    val cotizaciones by viewModel.cotizaciones.collectAsStateWithLifecycle()
    val filtro by viewModel.filtro.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text(if (sesion.rol == Rol.VENDEDOR) "Mis cotizaciones" else "Cotizaciones del equipo") }) },
        floatingActionButton = {
            // Solo el vendedor arma cotizaciones nuevas
            if (sesion.rol == Rol.VENDEDOR) {
                ExtendedFloatingActionButton(
                    onClick = onNueva,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Nueva cotización") }
                )
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Filtros por estado
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                EstadoCotizacion.entries.forEach { estado ->
                    FilterChip(
                        selected = filtro == estado,
                        onClick = { viewModel.onFiltroChange(estado) },
                        label = { Text(estado.etiqueta) },
                        leadingIcon = if (filtro == estado) {
                            { Icon(Icons.Filled.Check, contentDescription = null) }
                        } else null
                    )
                }
            }
            val lista = cotizaciones
            when {
                lista == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                lista.isEmpty() -> EstadoVacio(
                    icono = Icons.Filled.Description,
                    titulo = "No hay cotizaciones",
                    mensaje = if (filtro == null) "Crea una cotización y envíala en la misma visita." else "No hay cotizaciones en estado ${filtro?.etiqueta}."
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(lista, key = { it.id }) { cot ->
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
