package cl.duoc.tratohecho.ui.screens.ajustes

import androidx.compose.runtime.remember
import cl.duoc.tratohecho.utils.RutVisualTransformation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.tratohecho.ui.components.BarraConVolver
import cl.duoc.tratohecho.ui.components.BotonCarga
import cl.duoc.tratohecho.ui.components.CampoTexto
import cl.duoc.tratohecho.ui.components.MensajeError
import cl.duoc.tratohecho.viewmodel.EmpresaFormViewModel

@Composable
fun EmpresaFormScreen(
    mostrarMensaje: (String) -> Unit,
    onVolver: () -> Unit,
    viewModel: EmpresaFormViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.guardado.collect {
            mostrarMensaje(it)
            onVolver()
        }
    }

    Scaffold(topBar = { BarraConVolver("Datos de la empresa", onVolver) }) { padding ->
        if (estado.cargando) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                "Estos datos identifican a tu empresa como emisor en cada cotización.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            CampoTexto(
                estado.razonSocial, viewModel::onRazonSocialChange, "Razón social", Icons.Filled.Business,
                error = estado.errores.razonSocial, mostrarValido = estado.intentoGuardar
            )
            CampoTexto(
                estado.rut, viewModel::onRutChange, "RUT", Icons.Filled.Badge,
                error = estado.errores.rut, mostrarValido = estado.intentoGuardar,
                transformacion = remember { RutVisualTransformation() }
            )
            CampoTexto(
                estado.giro, viewModel::onGiroChange, "Giro", Icons.Filled.Storefront,
                error = estado.errores.giro, mostrarValido = estado.intentoGuardar
            )
            CampoTexto(
                estado.direccion, viewModel::onDireccionChange, "Dirección", Icons.Filled.LocationOn,
                error = estado.errores.direccion, mostrarValido = estado.intentoGuardar
            )
            CampoTexto(
                estado.correo, viewModel::onCorreoChange, "Correo", Icons.Filled.Email,
                error = estado.errores.correo, mostrarValido = estado.intentoGuardar, tipoTeclado = KeyboardType.Email
            )
            CampoTexto(
                estado.telefono, viewModel::onTelefonoChange, "Teléfono", Icons.Filled.Phone,
                error = estado.errores.telefono, mostrarValido = estado.intentoGuardar, tipoTeclado = KeyboardType.Phone
            )
            MensajeError(estado.errorGeneral)
            BotonCarga("Guardar cambios", viewModel::guardar, estado.guardando, icono = Icons.Filled.Save, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
