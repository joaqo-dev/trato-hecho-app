package cl.duoc.tratohecho.ui.screens.catalogo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Straighten
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
import cl.duoc.tratohecho.ui.components.CampoSelector
import cl.duoc.tratohecho.ui.components.CampoTexto
import cl.duoc.tratohecho.ui.components.MensajeError
import cl.duoc.tratohecho.viewmodel.ProductoFormViewModel

@Composable
fun ProductoFormScreen(
    mostrarMensaje: (String) -> Unit,
    onVolver: () -> Unit,
    viewModel: ProductoFormViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val unidades by viewModel.unidades.collectAsStateWithLifecycle()
    val impuestos by viewModel.impuestos.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.guardado.collect {
            mostrarMensaje(it)
            onVolver()
        }
    }

    Scaffold(topBar = { BarraConVolver(if (estado.esNuevo) "Nuevo producto" else "Editar producto", onVolver) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            CampoTexto(
                estado.nombre, viewModel::onNombreChange, "Nombre", Icons.Filled.Inventory2,
                error = estado.errores.nombre, mostrarValido = estado.intentoGuardar
            )
            CampoTexto(
                estado.descripcion, viewModel::onDescripcionChange, "Descripción (opcional)", Icons.AutoMirrored.Filled.Notes,
                error = estado.errores.descripcion, lineas = 2
            )
            CampoTexto(
                estado.precio, viewModel::onPrecioChange, "Precio neto (CLP)", Icons.Filled.AttachMoney,
                error = estado.errores.precio, mostrarValido = estado.intentoGuardar, tipoTeclado = KeyboardType.Number,
                ayuda = "Precio sin impuesto, en pesos"
            )
            CampoSelector(
                etiqueta = "Unidad",
                icono = Icons.Filled.Straighten,
                opciones = unidades,
                seleccionado = unidades.find { it.id == estado.unidadId },
                textoDe = { "${it.nombre} (${it.abreviatura})" },
                onSeleccionar = viewModel::onUnidadChange,
                error = estado.errores.unidad
            )
            CampoSelector(
                etiqueta = "Impuesto",
                icono = Icons.Filled.Percent,
                opciones = impuestos,
                seleccionado = impuestos.find { it.id == estado.impuestoId },
                textoDe = { "${it.nombre} (${it.porcentaje.toInt()}%)" },
                onSeleccionar = viewModel::onImpuestoChange,
                error = estado.errores.impuesto
            )
            MensajeError(estado.errorGeneral)
            BotonCarga("Guardar producto", viewModel::guardar, estado.guardando, icono = Icons.Filled.Save, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
