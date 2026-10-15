package cl.duoc.tratohecho.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// Selector reutilizable: se ve como un campo del formulario y al tocarlo abre una hoja inferior
// con las opciones (más cómoda en el teléfono que un menú desplegable).
// Si hay muchas opciones, la hoja trae un buscador.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> CampoSelector(
    etiqueta: String,
    icono: ImageVector,
    opciones: List<T>,
    seleccionado: T?,
    textoDe: (T) -> String,
    onSeleccionar: (T) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    habilitado: Boolean = true,
    textoSecundarioDe: ((T) -> String)? = null
) {
    var abierto by remember { mutableStateOf(false) }

    Box(modifier) {
        OutlinedTextField(
            value = seleccionado?.let(textoDe) ?: "",
            onValueChange = {},
            readOnly = true,
            enabled = habilitado,
            label = { Text(etiqueta) },
            leadingIcon = { Icon(icono, contentDescription = null) },
            trailingIcon = {
                if (error != null) Icon(Icons.Filled.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                else Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null)
            },
            isError = error != null,
            supportingText = { if (error != null) Text(error) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier.fillMaxWidth()
        )
        // Capa transparente que recibe el toque sobre todo el campo
        Box(
            Modifier
                .matchParentSize()
                .clip(MaterialTheme.shapes.medium)
                .clickable(enabled = habilitado) { abierto = true }
        )
    }

    if (abierto) {
        val estadoHoja = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val scope = rememberCoroutineScope()
        var busqueda by remember { mutableStateOf("") }
        val filtradas = if (busqueda.isBlank()) opciones
        else opciones.filter { opcion ->
            textoDe(opcion).contains(busqueda, ignoreCase = true) ||
                (textoSecundarioDe?.invoke(opcion)?.contains(busqueda, ignoreCase = true) ?: false)
        }

        ModalBottomSheet(
            onDismissRequest = { abierto = false },
            sheetState = estadoHoja,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                Modifier
                    .padding(horizontal = 20.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
            ) {
                Text(etiqueta, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 12.dp))
                if (opciones.size > 6) {
                    CampoBusqueda(busqueda, { busqueda = it }, "Buscar", Modifier.padding(bottom = 8.dp))
                }
                if (filtradas.isEmpty()) {
                    Text(
                        if (opciones.isEmpty()) "No hay opciones disponibles" else "Sin resultados para \"$busqueda\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
                LazyColumn(Modifier.heightIn(max = 440.dp)) {
                    items(filtradas) { opcion ->
                        val elegido = opcion == seleccionado
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(if (elegido) MaterialTheme.colorScheme.surfaceContainer else Color.Transparent)
                                .selectable(selected = elegido, role = Role.RadioButton) {
                                    onSeleccionar(opcion)
                                    scope.launch { estadoHoja.hide() }.invokeOnCompletion { abierto = false }
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    textoDe(opcion),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (elegido) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                textoSecundarioDe?.let {
                                    Text(
                                        it(opcion),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            if (elegido) {
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.Filled.Check, contentDescription = "Seleccionado")
                            }
                        }
                    }
                }
            }
        }
    }
}
