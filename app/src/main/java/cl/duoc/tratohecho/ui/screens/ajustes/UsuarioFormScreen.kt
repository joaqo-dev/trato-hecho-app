package cl.duoc.tratohecho.ui.screens.ajustes

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
                    Rol.VENDEDOR -> "Gestiona sus propios clientes y cotizaciones."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp, bottom = 8.dp)
            )

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("Usuario activo", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Un usuario desactivado no puede iniciar sesión",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = estado.activo, onCheckedChange = viewModel::onActivoChange, enabled = !estado.esUsuarioActual)
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
}
