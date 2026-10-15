package cl.duoc.tratohecho.ui.screens.clientes

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.tratohecho.ui.components.BarraConVolver
import cl.duoc.tratohecho.ui.components.BotonCarga
import cl.duoc.tratohecho.ui.components.CampoTexto
import cl.duoc.tratohecho.ui.components.TituloSeccion
import cl.duoc.tratohecho.viewmodel.ClienteFormViewModel
import cl.duoc.tratohecho.viewmodel.ContactoFormState

@Composable
fun ClienteFormScreen(
    mostrarMensaje: (String) -> Unit,
    onVolver: () -> Unit,
    viewModel: ClienteFormViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.guardado.collect { mensaje ->
            mostrarMensaje(mensaje)
            onVolver()
        }
    }

    Scaffold(
        topBar = { BarraConVolver(if (estado.esNuevo) "Nuevo cliente" else "Editar cliente", onVolver) }
    ) { padding ->
        if (estado.cargando) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            TituloSeccion("Datos del cliente")
            CampoTexto(
                valor = estado.rut,
                onValorChange = viewModel::onRutChange,
                etiqueta = "RUT",
                icono = Icons.Filled.Badge,
                error = estado.errores.rut,
                mostrarValido = estado.intentoGuardar,
                ayuda = "Ej: 76.123.456-0",
                modifier = Modifier.onFocusChanged { if (!it.isFocused) viewModel.formatearRut() }
            )
            CampoTexto(
                valor = estado.razonSocial,
                onValorChange = viewModel::onRazonSocialChange,
                etiqueta = "Razón social o nombre",
                icono = Icons.Filled.Business,
                error = estado.errores.razonSocial,
                mostrarValido = estado.intentoGuardar
            )
            CampoTexto(
                valor = estado.giro,
                onValorChange = viewModel::onGiroChange,
                etiqueta = "Giro",
                icono = Icons.Filled.Storefront,
                error = estado.errores.giro,
                mostrarValido = estado.intentoGuardar
            )
            CampoTexto(
                valor = estado.direccion,
                onValorChange = viewModel::onDireccionChange,
                etiqueta = "Dirección",
                icono = Icons.Filled.LocationOn,
                error = estado.errores.direccion,
                mostrarValido = estado.intentoGuardar
            )
            CampoTexto(
                valor = estado.correo,
                onValorChange = viewModel::onCorreoChange,
                etiqueta = "Correo",
                icono = Icons.Filled.Email,
                error = estado.errores.correo,
                mostrarValido = estado.intentoGuardar,
                tipoTeclado = KeyboardType.Email
            )
            CampoTexto(
                valor = estado.telefono,
                onValorChange = viewModel::onTelefonoChange,
                etiqueta = "Teléfono",
                icono = Icons.Filled.Phone,
                error = estado.errores.telefono,
                mostrarValido = estado.intentoGuardar,
                tipoTeclado = KeyboardType.Phone
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                TituloSeccion("Contactos (${estado.contactos.size})", modifier = Modifier.weight(1f))
                OutlinedButton(onClick = viewModel::abrirNuevoContacto) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text("Agregar")
                }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .animateContentSize()
                    .padding(vertical = 8.dp)
            ) {
                if (estado.contactos.isEmpty()) {
                    Text(
                        "Agrega a la persona que recibe las cotizaciones (opcional)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                estado.contactos.forEach { contacto ->
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 14.dp)) {
                            Column(Modifier.weight(1f).padding(vertical = 10.dp)) {
                                Text(contacto.nombre, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${contacto.cargo} · ${contacto.correo}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { viewModel.editarContacto(contacto) }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Editar contacto")
                            }
                            IconButton(onClick = { viewModel.eliminarContacto(contacto) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Quitar contacto")
                            }
                        }
                    }
                }
            }

            BotonCarga(
                texto = "Guardar cliente",
                onClick = viewModel::guardar,
                cargando = estado.guardando,
                icono = Icons.Filled.Save,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }

    estado.dialogoContacto?.let { dialogo ->
        DialogoContacto(
            estado = dialogo,
            onNombreChange = viewModel::onContactoNombreChange,
            onCargoChange = viewModel::onContactoCargoChange,
            onCorreoChange = viewModel::onContactoCorreoChange,
            onTelefonoChange = viewModel::onContactoTelefonoChange,
            onGuardar = viewModel::guardarContacto,
            onCancelar = viewModel::cerrarContacto
        )
    }
}

// Formulario de contacto dentro de un diálogo
@Composable
private fun DialogoContacto(
    estado: ContactoFormState,
    onNombreChange: (String) -> Unit,
    onCargoChange: (String) -> Unit,
    onCorreoChange: (String) -> Unit,
    onTelefonoChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(if (estado.esNuevo) "Nuevo contacto" else "Editar contacto") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                CampoTexto(estado.nombre, onNombreChange, "Nombre", Icons.Filled.Person, error = estado.errores.nombre)
                CampoTexto(estado.cargo, onCargoChange, "Cargo", Icons.Filled.Work, error = estado.errores.cargo)
                CampoTexto(
                    estado.correo, onCorreoChange, "Correo", Icons.Filled.Email,
                    error = estado.errores.correo, tipoTeclado = KeyboardType.Email
                )
                CampoTexto(
                    estado.telefono, onTelefonoChange, "Teléfono", Icons.Filled.Phone,
                    error = estado.errores.telefono, tipoTeclado = KeyboardType.Phone
                )
            }
        },
        confirmButton = { TextButton(onClick = onGuardar) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}
