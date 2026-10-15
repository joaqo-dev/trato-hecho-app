package cl.duoc.tratohecho.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// Título de sección + una tarjeta gris con filas "dato : valor" (como "Delivery Details")
@Composable
fun BloqueDetalle(
    titulo: String,
    modifier: Modifier = Modifier,
    contenido: @Composable () -> Unit
) {
    Column(modifier.fillMaxWidth()) {
        TituloSeccion(titulo)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(horizontal = 18.dp, vertical = 8.dp)
        ) {
            contenido()
        }
    }
}

@Composable
fun FilaDetalle(dato: String, valor: String, colorValor: Color = Color.Unspecified) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp)) {
        Text(
            dato,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 16.dp)
        )
        Text(
            valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = colorValor,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}
