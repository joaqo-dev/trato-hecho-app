package cl.duoc.tratohecho.ui.screens.inicio

import cl.duoc.tratohecho.ui.components.ContenidoActualizable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.tratohecho.domain.model.EstadoCotizacion
import cl.duoc.tratohecho.domain.model.Indicadores
import cl.duoc.tratohecho.domain.model.Rol
import cl.duoc.tratohecho.domain.model.Sesion
import cl.duoc.tratohecho.ui.components.AtajoBusqueda
import cl.duoc.tratohecho.ui.components.Avatar
import cl.duoc.tratohecho.ui.components.DialogoConfirmacion
import cl.duoc.tratohecho.ui.components.ESPACIO_BARRA_NAVEGACION
import cl.duoc.tratohecho.ui.components.EstadoVacio
import cl.duoc.tratohecho.ui.components.TarjetaCotizacion
import cl.duoc.tratohecho.ui.components.TituloSeccion
import cl.duoc.tratohecho.ui.components.efectoPresion
import cl.duoc.tratohecho.ui.theme.DURACION_CORTA
import cl.duoc.tratohecho.ui.theme.EaseOutFuerte
import cl.duoc.tratohecho.ui.theme.Pildora
import cl.duoc.tratohecho.ui.theme.TratoTheme
import cl.duoc.tratohecho.utils.Formatos
import cl.duoc.tratohecho.viewmodel.InicioViewModel

@Composable
fun InicioScreen(
    sesion: Sesion,
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
    val sincronizando by viewModel.sincronizando.collectAsStateWithLifecycle()
    var confirmarSalida by remember { mutableStateOf(false) }
    var montosOcultos by rememberSaveable { mutableStateOf(false) }
    val esVendedor = sesion.rol == Rol.VENDEDOR

    val datos = estado
    if (datos == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    ContenidoActualizable(sincronizando, onSincronizar, Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = ESPACIO_BARRA_NAVEGACION + 16.dp)
    ) {
        Encabezado(sesion = sesion, onSalir = { confirmarSalida = true })
        Spacer(Modifier.height(20.dp))

        TarjetaHero(
            indicadores = datos.indicadores,
            ocultos = montosOcultos,
            onAlternarOcultos = { montosOcultos = !montosOcultos },
            acciones = if (esVendedor) {
                listOf(
                    AccionHero("Cotizar", Icons.Filled.Add, onNuevaCotizacion),
                    AccionHero("Nuevo cliente", Icons.Filled.PersonAdd, onNuevoCliente)
                )
            } else {
                listOf(
                    AccionHero("Cotizaciones", Icons.Filled.Description, onAbrirCotizaciones),
                    AccionHero("Clientes", Icons.Filled.People, onAbrirClientes)
                )
            }
        )
        Spacer(Modifier.height(16.dp))
        AtajoBusqueda("Buscar cotización o cliente", onClick = onAbrirCotizaciones)

        TituloSeccion(if (esVendedor) "Cotización en curso" else "Última en seguimiento", modifier = Modifier.padding(top = 12.dp))
        val enCurso = datos.enCurso
        if (enCurso != null) {
            TarjetaCotizacion(
                cotizacion = enCurso,
                onClick = { onAbrirCotizacion(enCurso.id) },
                mostrarVendedor = !esVendedor,
                mostrarTracker = true
            )
        } else {
            EstadoVacio(
                icono = Icons.Filled.Description,
                titulo = "Nada en curso",
                mensaje = if (esVendedor) "Crea una cotización y envíala en la misma visita." else "No hay cotizaciones esperando respuesta."
            )
        }

        val recientes = datos.recientes.filter { it.id != enCurso?.id }.take(2)
        if (recientes.isNotEmpty()) {
            TituloSeccion("Actividad reciente", accion = "Ver todas", onAccion = onAbrirCotizaciones)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                recientes.forEach { cot ->
                    TarjetaCotizacion(cot, onClick = { onAbrirCotizacion(cot.id) }, mostrarVendedor = !esVendedor)
                }
            }
        }

        TituloSeccion(if (esVendedor) "Tu resumen" else "Resumen de la empresa")
        TarjetaResumen(datos.indicadores, montosOcultos)
        if (!esVendedor) {
            Spacer(Modifier.height(12.dp))
            TarjetaRanking(datos.indicadores, montosOcultos)
        }
    }
    }

    if (confirmarSalida) {
        DialogoConfirmacion(
            titulo = "Cerrar sesión",
            mensaje = if (pendientes > 0) {
                "Tienes $pendientes cambio(s) sin sincronizar que se perderán. Conéctate a internet antes de salir si quieres conservarlos."
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

@Composable
private fun Encabezado(sesion: Sesion, onSalir: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Avatar(sesion.nombre)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "Hola, ${sesion.nombre.substringBefore(" ")}",
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1
            )
            Text(
                "${sesion.empresaNombre} · ${sesion.rol.etiqueta}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        // Para actualizar se desliza la pantalla hacia abajo
        BotonCircular(Icons.AutoMirrored.Filled.Logout, "Cerrar sesión", onSalir)
    }
}

@Composable
private fun BotonCircular(icono: ImageVector, descripcion: String, onClick: () -> Unit) {
    FilledIconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.size(48.dp)
    ) {
        Icon(icono, contentDescription = descripcion)
    }
}

private data class AccionHero(val texto: String, val icono: ImageVector, val onClick: () -> Unit)

// Tarjeta principal negra: lo ganado este mes y lo que requiere atención hoy
// (cotizaciones esperando respuesta, las que vencen pronto y los borradores sin enviar)
@Composable
private fun TarjetaHero(
    indicadores: Indicadores,
    ocultos: Boolean,
    onAlternarOcultos: () -> Unit,
    acciones: List<AccionHero>
) {
    val colores = TratoTheme.colores
    val monto by animateFloatAsState(
        indicadores.montoGanadoMes.toFloat(),
        animationSpec = tween(900, easing = EaseOutFuerte),
        label = "monto_ganado"
    )
    // Al terminar la animación se muestra el valor exacto (Float pierde precisión en montos grandes)
    val montoVisible = if (monto == indicadores.montoGanadoMes.toFloat()) indicadores.montoGanadoMes else monto.toDouble()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(colores.hero)
            .border(1.dp, colores.bordeHero, MaterialTheme.shapes.extraLarge)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text("Ganado este mes", style = MaterialTheme.typography.bodyMedium, color = colores.enHeroSuave)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AnimatedContent(
                        targetState = ocultos,
                        transitionSpec = { fadeIn(tween(DURACION_CORTA)) togetherWith fadeOut(tween(120)) },
                        label = "ocultar_monto"
                    ) { estaOculto ->
                        Text(
                            if (estaOculto) "$ ••••••" else Formatos.monto(montoVisible),
                            style = MaterialTheme.typography.displaySmall,
                            color = colores.enHero,
                            maxLines = 1
                        )
                    }
                    IconButton(onClick = onAlternarOcultos) {
                        Icon(
                            if (ocultos) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = if (ocultos) "Mostrar montos" else "Ocultar montos",
                            tint = colores.enHeroSuave
                        )
                    }
                }
            }
            Text(
                if (indicadores.ganadasMes == 1) "1 aceptada" else "${indicadores.ganadasMes} aceptadas",
                style = MaterialTheme.typography.labelMedium,
                color = colores.enHeroSuave,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Lo que requiere atención
        Spacer(Modifier.height(14.dp))
        Row(Modifier.height(IntrinsicSize.Min)) {
            CifraHero(
                valor = indicadores.porResponder.toString(),
                etiqueta = "Por responder",
                primera = true,
                detalle = if (ocultos) "$ •••" else Formatos.monto(indicadores.montoPorResponder),
                modifier = Modifier.weight(1.3f)
            )
            VerticalDivider(Modifier.fillMaxHeight(), color = colores.enHeroSuave.copy(alpha = 0.3f))
            CifraHero(
                valor = indicadores.vencenPronto.toString(),
                etiqueta = "Por vencer",
                detalle = "en 3 días",
                destacado = indicadores.vencenPronto > 0,
                modifier = Modifier.weight(1f)
            )
            VerticalDivider(Modifier.fillMaxHeight(), color = colores.enHeroSuave.copy(alpha = 0.3f))
            CifraHero(
                valor = indicadores.borradores.toString(),
                etiqueta = "Borradores",
                detalle = "por enviar",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            acciones.forEach { accion ->
                BotonHero(accion, Modifier.weight(1f))
            }
        }
    }
}

// Cifra chica dentro de la tarjeta negra. "destacado" la pinta en ámbar (requiere atención).
@Composable
private fun CifraHero(
    valor: String,
    etiqueta: String,
    detalle: String,
    modifier: Modifier = Modifier,
    destacado: Boolean = false,
    primera: Boolean = false   // la primera cifra se alinea con el borde de la tarjeta
) {
    val colores = TratoTheme.colores
    Column(modifier.padding(start = if (primera) 0.dp else 12.dp, end = 8.dp)) {
        Text(
            valor,
            style = MaterialTheme.typography.titleLarge,
            color = if (destacado) MaterialTheme.colorScheme.secondary else colores.enHero
        )
        Text(etiqueta, style = MaterialTheme.typography.labelMedium, color = colores.enHero, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(detalle, style = MaterialTheme.typography.labelSmall, color = colores.enHeroSuave, maxLines = 1)
    }
}

@Composable
private fun BotonHero(accion: AccionHero, modifier: Modifier = Modifier) {
    val colores = TratoTheme.colores
    val interaccion = remember { MutableInteractionSource() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .height(52.dp)
            .efectoPresion(interaccion)
            .clip(Pildora)
            .background(colores.botonHero)
            .clickable(interactionSource = interaccion, indication = null, onClick = accion.onClick)
            .padding(horizontal = 12.dp)
    ) {
        Icon(accion.icono, contentDescription = null, tint = colores.enBotonHero, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            accion.texto,
            style = MaterialTheme.typography.labelLarge,
            color = colores.enBotonHero,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// Tres cifras en una fila separadas por líneas: menos ruido que tres tarjetas iguales
@Composable
private fun TarjetaResumen(indicadores: Indicadores, ocultos: Boolean) {
    val tasa by animateFloatAsState(indicadores.tasaAceptacion.toFloat(), tween(900, easing = EaseOutFuerte), label = "tasa")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(vertical = 16.dp)
    ) {
        Cifra("Aceptación", Formatos.porcentaje(tasa.toDouble()), Modifier.weight(1f))
        VerticalDivider(Modifier.fillMaxHeight(), color = MaterialTheme.colorScheme.outline)
        Cifra("Enviadas este mes", indicadores.cotizacionesDelMes.toString(), Modifier.weight(1f))
        VerticalDivider(Modifier.fillMaxHeight(), color = MaterialTheme.colorScheme.outline)
        Cifra("Facturado", if (ocultos) "$ •••" else Formatos.monto(indicadores.montoFacturado), Modifier.weight(1.3f))
    }
}

@Composable
private fun Cifra(etiqueta: String, valor: String, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.padding(horizontal = 6.dp)) {
        Text(valor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(etiqueta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
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
            "Monto de cotizaciones aceptadas y facturadas",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (datos.rankingVendedores.isEmpty()) {
            Text("Aún no hay cotizaciones aceptadas", style = MaterialTheme.typography.bodyMedium)
        }
        datos.rankingVendedores.forEachIndexed { i, vendedor ->
            if (i > 0) HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                Avatar(vendedor.nombre)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(vendedor.nombre, style = MaterialTheme.typography.titleSmall)
                        if (i == 0) {
                            Spacer(Modifier.width(6.dp))
                            Icon(Icons.Filled.EmojiEvents, contentDescription = "Primer lugar", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                        }
                    }
                    Text(
                        "${vendedor.cantidadGanadas} ganada(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(if (ocultos) "$ •••" else Formatos.monto(vendedor.montoGanado), style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}
