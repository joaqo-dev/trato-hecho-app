package cl.duoc.tratohecho.ui.screens.catalogo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.tratohecho.ui.components.BarraConVolver
import cl.duoc.tratohecho.ui.components.BotonCarga
import cl.duoc.tratohecho.ui.components.CampoTexto
import cl.duoc.tratohecho.ui.components.MensajeError
import cl.duoc.tratohecho.viewmodel.ImpuestoFormViewModel

@Composable
fun ImpuestoFormScreen(
    mostrarMensaje: (String) -> Unit,
    onVolver: () -> Unit,
    viewModel: ImpuestoFormViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.guardado.collect {
            mostrarMensaje(it)
            onVolver()
        }
    }

    Scaffold(topBar = { BarraConVolver(if (estado.esNuevo) "Nuevo impuesto" else "Editar impuesto", onVolver) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            CampoTexto(
                estado.nombre, viewModel::onNombreChange, "Nombre", Icons.AutoMirrored.Filled.Label,
                error = estado.errorNombre, mostrarValido = estado.intentoGuardar, ayuda = "Ej: IVA"
            )
            CampoTexto(
                estado.porcentaje, viewModel::onPorcentajeChange, "Porcentaje", Icons.Filled.Percent,
                error = estado.errorPorcentaje, mostrarValido = estado.intentoGuardar,
                tipoTeclado = KeyboardType.Decimal, ayuda = "Entre 0 y 100"
            )
            MensajeError(estado.errorGeneral)
            BotonCarga("Guardar impuesto", viewModel::guardar, estado.guardando, icono = Icons.Filled.Save, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
