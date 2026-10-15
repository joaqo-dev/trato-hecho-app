package cl.duoc.tratohecho.ui.screens.ajustes

import cl.duoc.tratohecho.viewmodel.TraspasoUiState
import cl.duoc.tratohecho.data.local.entity.UsuarioEntity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.draw.clip
import androidx.compose.material3.TextButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import cl.duoc.tratohecho.domain.model.Rol
import cl.duoc.tratohecho.ui.components.BarraConVolver
import cl.duoc.tratohecho.ui.components.BotonCarga
import cl.duoc.tratohecho.ui.components.CampoTexto
import cl.duoc.tratohecho.ui.components.MensajeError
import cl.duoc.tratohecho.ui.components.SelectorOpciones
import cl.duoc.tratohecho.ui.components.TituloSeccion
import cl.duoc.tratohecho.viewmodel.UsuarioFormViewModel

@Composable
fun UsuarioFormScreen(
    mostrarMensaje: (String) -> Unit,
    onVolver: () -> Unit,
    viewModel: UsuarioFormViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val vendedores by viewModel.vendedoresDisponibles.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.guardado.collect {
            mostrarMensaje(it)
            onVolver()
        }
    }

    Scaffold(topBar = { BarraConVolver(if (estado.esNuevo) "Nuevo usuario" else "Editar usuario", onVolver) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            CampoTexto(
                estado.nombre, viewModel::onNombreChange, "Nombre completo", Icons.Filled.Person,
                error = estado.errores.nombre, mostrarValido = estado.intentoGuardar
            )
            CampoTexto(
                estado.correo, viewModel::onCorreoChange, "Correo (usuario de acceso)", Icons.Filled.Email,
                error = estado.errores.correo, mostrarValido = estado.intentoGuardar, tipoTeclado = KeyboardType.Email
            )

            TituloSeccion("Perfil")
            SelectorOpciones(
                opciones = Rol.entries,
                seleccionada = estado.rol,
                textoDe = { it.etiqueta },
                onSeleccionar = viewModel::onRolChange,
                habilitado = !estado.esUsuarioActual
            )
            Text(
                when (estado.rol) {
                    Rol.ADMINISTRADOR -> "Parametriza catálogo, impuestos y usuarios. Ve todas las cotizaciones."
                    Rol.SUPERVISOR -> "Revisa y hace seguimiento de las cotizaciones de los vendedores."
                    Rol.VENDEDOR -> "Cotiza a los clientes que el administrador le asigna y actualiza su información de contacto."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp, bottom = 8.dp)
            )

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("Usuario activo", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Desactivarlo es darlo de baja: no puede iniciar sesión, pero sus datos e historial se conservan",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = estado.activo, onCheckedChange = viewModel::onActivoChange, enabled = !estado.esUsuarioActual)
            }
            // Advertencia: un vendedor con clientes no puede quedar de baja sin traspasarlos
            AnimatedVisibility(visible = estado.requiereTraspaso) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(14.dp)
                ) {
                    Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "${estado.nombre} tiene ${estado.clientesACargo} cliente(s) a cargo. Para mantener la operación, " +
                            "al guardar deberás reasignarlos a otro vendedor. Sus cotizaciones se conservan en el historial.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            TituloSeccion(if (estado.esNuevo) "Clave de acceso" else "Cambiar clave (opcional)")
            CampoTexto(
                estado.clave, viewModel::onClaveChange, "Clave", Icons.Filled.Lock,
                error = estado.errores.clave, esClave = true, tipoTeclado = KeyboardType.Password,
                ayuda = "Mínimo 8 caracteres, con letras y números"
            )
            CampoTexto(
                estado.confirmacion, viewModel::onConfirmacionChange, "Repetir clave", Icons.Filled.LockReset,
                error = estado.errores.confirmacion, esClave = true, tipoTeclado = KeyboardType.Password
            )
            MensajeError(estado.errorGeneral)
            BotonCarga("Guardar usuario", viewModel::guardar, estado.guardando, icono = Icons.Filled.Save, modifier = Modifier.padding(top = 8.dp))
        }
    }

    estado.traspaso?.let { traspaso ->
        DialogoTraspaso(
            nombre = estado.nombre,
            clientes = estado.clientesACargo,
            vendedores = vendedores,
            estado = traspaso,
            guardando = estado.guardando,
            onSeleccionar = viewModel::onVendedorTraspaso,
            onConfirmar = viewModel::confirmarTraspaso,
            onCancelar = viewModel::cerrarTraspaso
        )
    }
}

// Diálogo para reasignar todos los clientes del vendedor que se da de baja
@Composable
private fun DialogoTraspaso(
    nombre: String,
    clientes: Int,
    vendedores: List<UsuarioEntity>,
    estado: TraspasoUiState,
    guardando: Boolean,
    onSeleccionar: (String) -> Unit,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        icon = { Icon(Icons.Filled.WarningAmber, contentDescription = null) },
        title = { Text("Reasignar clientes") },
        text = {
            Column {
                Text(
                    "$nombre tiene $clientes cliente(s) a cargo. Elige qué vendedor los atenderá desde ahora. " +
                        "No se borra nada: sus cotizaciones quedan en el historial.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(10.dp))
                if (vendedores.isEmpty()) {
                    Text(
                        "No hay otro vendedor activo. Crea o activa uno antes de dar de baja a $nombre.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                vendedores.forEach { vendedor ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = estado.seleccionadoId == vendedor.id,
                                onClick = { onSeleccionar(vendedor.id) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(selected = estado.seleccionadoId == vendedor.id, onClick = null)
                        Column(Modifier.padding(start = 8.dp)) {
                            Text(vendedor.nombre, style = MaterialTheme.typography.bodyLarge)
                            Text(vendedor.correo, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                MensajeError(estado.error)
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirmar, enabled = !guardando && vendedores.isNotEmpty()) {
                Text(if (guardando) "Guardando…" else "Reasignar y dar de baja")
            }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}
