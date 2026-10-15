package cl.duoc.tratohecho.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

// Contacto rápido: avatar con iniciales + botones redondos para escribir y llamar
@Composable
fun TarjetaContacto(
    nombre: String,
    descripcion: String,
    onCorreo: (() -> Unit)?,
    onLlamar: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Avatar(nombre)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(nombre, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (onCorreo != null) {
            FilledIconButton(
                onClick = onCorreo,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .size(48.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            ) {
                Icon(Icons.Outlined.Email, contentDescription = "Escribir correo a $nombre")
            }
            Spacer(Modifier.width(8.dp))
        }
        if (onLlamar != null) {
            FilledIconButton(onClick = onLlamar, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Outlined.Call, contentDescription = "Llamar a $nombre")
            }
        }
    }
}

// Círculo con las iniciales de una persona o empresa
@Composable
fun Avatar(nombre: String, modifier: Modifier = Modifier) {
    val iniciales = nombre.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1).uppercase() }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurface)
    ) {
        Text(iniciales, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.surface)
    }
}
