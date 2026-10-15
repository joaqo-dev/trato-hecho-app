package cl.duoc.tratohecho.ui.screens.catalogo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShortText
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.tratohecho.ui.components.BarraConVolver
import cl.duoc.tratohecho.ui.components.BotonCarga
import cl.duoc.tratohecho.ui.components.CampoTexto
import cl.duoc.tratohecho.ui.components.MensajeError
import cl.duoc.tratohecho.viewmodel.UnidadFormViewModel

@Composable
fun UnidadFormScreen(
    mostrarMensaje: (String) -> Unit,
    onVolver: () -> Unit,
    viewModel: UnidadFormViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.guardado.collect {
            mostrarMensaje(it)
            onVolver()
        }
    }

    Scaffold(topBar = { BarraConVolver(if (estado.esNuevo) "Nueva unidad" else "Editar unidad", onVolver) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            CampoTexto(
                estado.nombre, viewModel::onNombreChange, "Nombre", Icons.Filled.Straighten,
                error = estado.errorNombre, mostrarValido = estado.intentoGuardar, ayuda = "Ej: Metro cuadrado"
            )
            CampoTexto(
                estado.abreviatura, viewModel::onAbreviaturaChange, "Abreviatura", Icons.AutoMirrored.Filled.ShortText,
                error = estado.errorAbreviatura, mostrarValido = estado.intentoGuardar, ayuda = "Ej: m²"
            )
            MensajeError(estado.errorGeneral)
            BotonCarga("Guardar unidad", viewModel::guardar, estado.guardando, icono = Icons.Filled.Save, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
