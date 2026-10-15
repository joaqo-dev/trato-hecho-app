package cl.duoc.tratohecho.ui.screens.clientes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.tratohecho.domain.model.Rol
import cl.duoc.tratohecho.domain.model.Sesion
import cl.duoc.tratohecho.ui.components.BarraConVolver
import cl.duoc.tratohecho.ui.components.DialogoConfirmacion
import cl.duoc.tratohecho.ui.components.FilaDato
import cl.duoc.tratohecho.ui.components.TarjetaCotizacion
import cl.duoc.tratohecho.ui.components.TituloSeccion
import cl.duoc.tratohecho.viewmodel.ClienteDetalleViewModel
import cl.duoc.tratohecho.viewmodel.EventoDetalleCliente

@Composable
fun ClienteDetalleScreen(
    sesion: Sesion,
    mostrarMensaje: (String) -> Unit,
    onVolver: () -> Unit,
    onEditar: (String) -> Unit,
    onAbrirCotizacion: (String) -> Unit,
    viewModel: ClienteDetalleViewModel = hiltViewModel()
) {
    val cliente by viewModel.cliente.collectAsStateWithLifecycle()
    val cotizaciones by viewModel.cotizaciones.collectAsStateWithLifecycle()
    var confirmarEliminar by remember { mutableStateOf(false) }
    // El administrador solo consulta; vendedor y supervisor pueden modificar
    val puedeEditar = sesion.rol != Rol.ADMINISTRADOR

    LaunchedEffect(Unit) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                is EventoDetalleCliente.Mensaje -> mostrarMensaje(evento.texto)
                EventoDetalleCliente.Eliminado -> {
                    mostrarMensaje("Cliente eliminado")
                    onVolver()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            BarraConVolver(
                titulo = cliente?.cliente?.razonSocial ?: "Cliente",
                onVolver = onVolver,
                acciones = {
                    if (puedeEditar && cliente != null) {
                        IconButton(onClick = { onEditar(cliente!!.cliente.id) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar")
                        }
                        IconButton(onClick = { confirmarEliminar = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                        }
                    }
                }
            )
        }
    ) { padding ->
        val datos = cliente
        if (datos == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    FilaDato(Icons.Filled.Badge, datos.cliente.rut)
                    FilaDato(Icons.Filled.Business, datos.cliente.giro)
                    FilaDato(Icons.Filled.LocationOn, datos.cliente.direccion)
                    FilaDato(Icons.Filled.Email, datos.cliente.correo)
                    FilaDato(Icons.Filled.Phone, datos.cliente.telefono)
                    if (datos.cliente.pendienteSync) {
                        Text(
                            "Pendiente de sincronizar",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }

            TituloSeccion("Contactos")
            if (datos.contactos.isEmpty()) {
                Text("Sin contactos registrados", style = MaterialTheme.typography.bodyMedium)
            }
            datos.contactos.forEach { contacto ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(14.dp)) {
                        FilaDato(Icons.Filled.Person, contacto.nombre)
                        FilaDato(Icons.Filled.Work, contacto.cargo)
                        HorizontalDivider(Modifier.padding(vertical = 4.dp))
                        FilaDato(Icons.Filled.Email, contacto.correo)
                        FilaDato(Icons.Filled.Phone, contacto.telefono)
                    }
                }
            }

            TituloSeccion("Cotizaciones (${cotizaciones.size})")
            if (cotizaciones.isEmpty()) {
                Text("Este cliente aún no tiene cotizaciones", style = MaterialTheme.typography.bodyMedium)
            }
            cotizaciones.forEach { cot ->
                TarjetaCotizacion(cot, onClick = { onAbrirCotizacion(cot.id) })
            }
        }
    }

    if (confirmarEliminar) {
        DialogoConfirmacion(
            titulo = "Eliminar cliente",
            mensaje = "Se eliminará el cliente y sus contactos. Solo es posible si no tiene cotizaciones. Requiere conexión.",
            textoConfirmar = "Eliminar",
            onConfirmar = {
                confirmarEliminar = false
                viewModel.eliminar()
            },
            onCancelar = { confirmarEliminar = false }
        )
    }
}
