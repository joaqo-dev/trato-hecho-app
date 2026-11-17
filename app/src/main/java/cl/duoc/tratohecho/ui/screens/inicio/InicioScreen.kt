package cl.duoc.tratohecho.ui.screens.inicio

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.tratohecho.domain.model.Indicadores
import cl.duoc.tratohecho.domain.model.Rol
import cl.duoc.tratohecho.domain.model.Sesion
import cl.duoc.tratohecho.ui.components.AtajoBusqueda
import cl.duoc.tratohecho.ui.components.Avatar
import cl.duoc.tratohecho.ui.components.BotonCarga
import cl.duoc.tratohecho.ui.components.ContenidoActualizable
import cl.duoc.tratohecho.ui.components.DialogoConfirmacion
import cl.duoc.tratohecho.ui.components.ESPACIO_BARRA_NAVEGACION
import cl.duoc.tratohecho.ui.components.EstadoVacio
import cl.duoc.tratohecho.ui.components.TarjetaCotizacion
import cl.duoc.tratohecho.ui.components.TituloSeccion
import cl.duoc.tratohecho.ui.components.colorEstado
import cl.duoc.tratohecho.ui.components.efectoPresion
import cl.duoc.tratohecho.domain.model.EstadoCotizacion
import cl.duoc.tratohecho.ui.theme.DURACION_CORTA
import cl.duoc.tratohecho.ui.theme.Pildora
import cl.duoc.tratohecho.utils.Formatos
import cl.duoc.tratohecho.viewmodel.Bandeja
import cl.duoc.tratohecho.viewmodel.InicioUiState
import cl.duoc.tratohecho.viewmodel.InicioViewModel

// Cuántas cotizaciones se muestran por bandeja antes de "Ver todas"
private const val MAX_POR_BANDEJA = 4

// El inicio responde una sola pregunta: ¿qué cotizaciones necesitan que haga algo hoy?
// Arriba la acción principal, luego las bandejas de trabajo y, al final, el resumen del mes.
@Composable
fun InicioScreen(
    sesion: Sesion,
    actualizando: Boolean,
    onSincronizar: () -> Unit,
    onCerrarSesion: () -> Unit,
    onNuevaCotizacion: () -> Unit,
    onNuevoCliente: () -> Unit,
    onAbrirCotizacion: (String) -> Unit,
    onAbrirCotizaciones: () -> Unit,
    onAbrirClientes: () -> Unit,
    viewModel: InicioViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val pendientes by viewModel.pendientes.collectAsStateWithLifecycle()
    var confirmarSalida by remember { mutableStateOf(false) }
    var montosOcultos by rememberSaveable { mutableStateOf(false) }
    val esVendedor = sesion.rol == Rol.VENDEDOR

    val datos = estado
    if (datos == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    ContenidoActualizable(actualizando, onSincronizar, Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = ESPACIO_BARRA_NAVEGACION + 16.dp)
        ) {
            Encabezado(sesion = sesion, onSalir = { confirmarSalida = true })
            Spacer(Modifier.height(20.dp))

            // Resumen del mes arriba, a la vista apenas se abre la app
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (esVendedor) "Tu mes" else "El mes de la empresa",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { montosOcultos = !montosOcultos }) {
                    Icon(
                        if (montosOcultos) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (montosOcultos) "Mostrar montos" else "Ocultar montos",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            ResumenMes(datos.indicadores, montosOcultos)
            Spacer(Modifier.height(20.dp))

            // Una sola acción principal por perfil
            when (sesion.rol) {
                Rol.VENDEDOR -> BotonCarga("Nueva cotización", onNuevaCotizacion, cargando = false, icono = Icons.Filled.Add)
                Rol.ADMINISTRADOR -> BotonCarga("Nuevo cliente", onNuevoCliente, cargando = false, icono = Icons.Filled.Add)
                Rol.SUPERVISOR -> Unit
            }
            if (sesion.rol != Rol.SUPERVISOR) Spacer(Modifier.height(12.dp))
            AtajoBusqueda("Buscar cotización o cliente", onClick = onAbrirCotizaciones)

            Spacer(Modifier.height(28.dp))
            SeccionBandejas(
                datos = datos,
                esVendedor = esVendedor,
                onAbrirCotizacion = onAbrirCotizacion,
                onVerTodas = onAbrirCotizaciones
            )

            if (!esVendedor) {
                Spacer(Modifier.height(28.dp))
                TarjetaRanking(datos.indicadores, montosOcultos)
            }
        }
    }

    if (confirmarSalida) {
        DialogoConfirmacion(
            titulo = "Cerrar sesión",
            mensaje = if (pendientes > 0) {
                "Tienes ${plural(pendientes, "cambio sin sincronizar", "cambios sin sincronizar")} que se perderán. " +
                    "Conéctate a internet antes de salir si quieres conservarlos."
            } else {
                "¿Quieres cerrar tu sesión en este teléfono?"
            },
            textoConfirmar = "Cerrar sesión",
            onConfirmar = {
                confirmarSalida = false
                onCerrarSesion()
            },
            onCancelar = { confirmarSalida = false }
        )
    }
}

// Saludo con la fecha de hoy. El avatar abre el menú de cuenta (cerrar sesión no
// queda como botón suelto en la pantalla más usada, para evitar toques por error).
@Composable
private fun Encabezado(sesion: Sesion, onSalir: () -> Unit) {
    var menuAbierto by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                Formatos.fechaLarga(System.currentTimeMillis()).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Hola, ${sesion.nombre.substringBefore(" ")}",
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box {
            IconButton(onClick = { menuAbierto = true }, modifier = Modifier.size(52.dp)) {
                Avatar(sesion.nombre, Modifier.size(44.dp))
            }
            DropdownMenu(
                expanded = menuAbierto,
                onDismissRequest = { menuAbierto = false },
                shape = MaterialTheme.shapes.medium,
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(sesion.nombre, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "${sesion.rol.etiqueta} · ${sesion.empresaNombre}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                DropdownMenuItem(
                    text = { Text("Cerrar sesión") },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                    onClick = {
                        menuAbierto = false
                        onSalir()
                    }
                )
            }
        }
    }
}

// Bandejas de trabajo: pestañas con el conteo de cada una y la lista de la elegida.
// El conteo siempre está a la vista (reconocer en vez de recordar) y la urgente se marca en ámbar.
@Composable
private fun SeccionBandejas(
    datos: InicioUiState,
    esVendedor: Boolean,
    onAbrirCotizacion: (String) -> Unit,
    onVerTodas: () -> Unit
) {
    // "En cola" solo aparece cuando hay algo: no tiene sentido mostrarla vacía
    val visibles = Bandeja.entries.filter { it != Bandeja.EN_COLA || !datos.bandejas[it].isNullOrEmpty() }
    var elegidaNombre by rememberSaveable { mutableStateOf(datos.bandejaInicial.name) }
    val elegida = Bandeja.valueOf(elegidaNombre).takeIf { it in visibles } ?: datos.bandejaInicial

    Text("Por atender", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(12.dp))

    // Sin nada pendiente, las pestañas en cero solo agregan ruido: un único mensaje basta
    if (datos.bandejas.values.all { it.isEmpty() }) {
        EstadoVacio(
            icono = Icons.Outlined.Inbox,
            titulo = if (!datos.hayCotizaciones) "Aún no hay cotizaciones" else "Todo al día",
            mensaje = when {
                !datos.hayCotizaciones && esVendedor -> "Crea tu primera cotización y envíala en la misma visita."
                !datos.hayCotizaciones -> "Aquí verás las cotizaciones que esperan respuesta o están por vencer."
                else -> "No hay cotizaciones por vencer, esperando respuesta ni borradores sin enviar."
            }
        )
        TextButton(onClick = onVerTodas, modifier = Modifier.fillMaxWidth()) {
            Text("Ver todas las cotizaciones", color = MaterialTheme.colorScheme.onSurface)
        }
        return
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        visibles.forEach { bandeja ->
            PestanaBandeja(
                bandeja = bandeja,
                cantidad = datos.bandejas[bandeja]?.size ?: 0,
                seleccionada = bandeja == elegida,
                onClick = { elegidaNombre = bandeja.name }
            )
        }
    }
    Spacer(Modifier.height(12.dp))

    // Cambio de pestaña sin desplazamientos: un fundido corto basta (se usa decenas de veces al día)
    AnimatedContent(
        targetState = elegida,
        transitionSpec = { fadeIn(tween(DURACION_CORTA)) togetherWith fadeOut(tween(120)) },
        label = "bandeja"
    ) { bandeja ->
        val lista = datos.bandejas[bandeja].orEmpty()
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (lista.isEmpty()) {
                EstadoVacio(
                    icono = Icons.Outlined.Inbox,
                    titulo = if (!datos.hayCotizaciones) "Aún no hay cotizaciones" else "Todo al día",
                    mensaje = if (!datos.hayCotizaciones && esVendedor) "Crea tu primera cotización y envíala en la misma visita."
                    else bandeja.vacio
                )
            } else {
                lista.take(MAX_POR_BANDEJA).forEach { cot ->
                    TarjetaCotizacion(cot, onClick = { onAbrirCotizacion(cot.id) }, mostrarVendedor = !esVendedor)
                }
                val resto = lista.size - MAX_POR_BANDEJA
                TextButton(onClick = onVerTodas, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        if (resto > 0) "Ver ${plural(resto, "cotización más", "cotizaciones más")}" else "Ver todas las cotizaciones",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun PestanaBandeja(bandeja: Bandeja, cantidad: Int, seleccionada: Boolean, onClick: () -> Unit) {
    val esquema = MaterialTheme.colorScheme
    val urgente = (bandeja == Bandeja.POR_VENCER || bandeja == Bandeja.EN_COLA) && cantidad > 0
    val interaccion = remember { MutableInteractionSource() }
    val fondo by animateColorAsState(
        if (seleccionada) esquema.primary else esquema.surfaceContainer, tween(DURACION_CORTA), label = "fondo_bandeja"
    )
    val texto by animateColorAsState(
        if (seleccionada) esquema.onPrimary else esquema.onSurface, tween(DURACION_CORTA), label = "texto_bandeja"
    )
    val colorUrgente = colorEstado(EstadoCotizacion.VENCIDA)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .efectoPresion(interaccion)
            .clip(Pildora)
            .background(fondo)
            .selectable(
                selected = seleccionada,
                onClick = onClick,
                role = Role.Tab,
                interactionSource = interaccion,
                indication = null
            )
            .defaultMinSize(minHeight = 44.dp)
            .padding(start = 16.dp, end = 8.dp)
    ) {
        Text(bandeja.titulo, style = MaterialTheme.typography.labelLarge, color = texto)
        Spacer(Modifier.width(8.dp))
        // Contador en una burbuja: ámbar si pide atención
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .defaultMinSize(minWidth = 26.dp, minHeight = 26.dp)
                .clip(Pildora)
                .background(
                    when {
                        urgente -> colorUrgente.fondo
                        seleccionada -> esquema.onPrimary.copy(alpha = 0.16f)
                        else -> esquema.surfaceContainerHighest
                    }
                )
                .padding(horizontal = 8.dp)
        ) {
            Text(
                cantidad.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (urgente) colorUrgente.texto else texto
            )
        }
    }
}

// Resumen del mes en una sola franja: tres cifras con su contexto, sin protagonismo
@Composable
private fun ResumenMes(indicadores: Indicadores, ocultos: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(vertical = 16.dp)
    ) {
        Cifra(
            etiqueta = plural(indicadores.ganadasMes, "aceptada", "aceptadas").let { "Ganado · $it" },
            valor = if (ocultos) "$ •••" else Formatos.monto(indicadores.montoGanadoMes),
            modifier = Modifier.weight(1.4f)
        )
        VerticalDivider(Modifier.fillMaxHeight(), color = MaterialTheme.colorScheme.outline)
        Cifra("Enviadas", indicadores.cotizacionesDelMes.toString(), Modifier.weight(1f))
        VerticalDivider(Modifier.fillMaxHeight(), color = MaterialTheme.colorScheme.outline)
        Cifra("Aceptación", Formatos.porcentaje(indicadores.tasaAceptacion), Modifier.weight(1f))
    }
}

@Composable
private fun Cifra(etiqueta: String, valor: String, modifier: Modifier = Modifier) {
    Column(modifier.padding(horizontal = 14.dp)) {
        Text(valor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
        Spacer(Modifier.height(2.dp))
        Text(
            etiqueta,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TarjetaRanking(datos: Indicadores, ocultos: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(18.dp)
    ) {
        Text("Ranking de vendedores", style = MaterialTheme.typography.titleSmall)
        Text(
            "Monto aceptado y facturado",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (datos.rankingVendedores.isEmpty()) {
            Text(
                "Aparecerá cuando un cliente acepte su primera cotización.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        datos.rankingVendedores.forEachIndexed { i, vendedor ->
            if (i > 0) HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                Avatar(vendedor.nombre, Modifier.size(40.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(vendedor.nombre, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (i == 0) {
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                Icons.Filled.EmojiEvents,
                                contentDescription = "Primer lugar",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        plural(vendedor.cantidadGanadas, "cotización ganada", "cotizaciones ganadas"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(if (ocultos) "$ •••" else Formatos.monto(vendedor.montoGanado), style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

// "1 cotización" / "3 cotizaciones"
private fun plural(n: Int, singular: String, pluralTexto: String) = "$n ${if (n == 1) singular else pluralTexto}"
