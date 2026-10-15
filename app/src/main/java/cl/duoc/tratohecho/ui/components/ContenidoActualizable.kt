package cl.duoc.tratohecho.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Contenido que se actualiza deslizando hacia abajo: vuelve a pedir los datos a la API
// y muestra la rueda de carga mientras tanto.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContenidoActualizable(
    actualizando: Boolean,
    onActualizar: () -> Unit,
    modifier: Modifier = Modifier,
    contenido: @Composable BoxScope.() -> Unit
) {
    val estado = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = actualizando,
        onRefresh = onActualizar,
        state = estado,
        modifier = modifier,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = estado,
                isRefreshing = actualizando,
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        },
        content = contenido
    )
}

// Rueda de carga con texto, para cuando los datos todavía vienen en camino desde la API
@Composable
fun CargandoDatos(texto: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 56.dp)
    ) {
        CircularProgressIndicator(Modifier.size(36.dp), strokeWidth = 3.dp)
        Text(texto, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
