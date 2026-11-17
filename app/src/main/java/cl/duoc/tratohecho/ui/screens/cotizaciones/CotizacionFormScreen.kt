package cl.duoc.tratohecho.ui.screens.cotizaciones

import cl.duoc.tratohecho.domain.CalculadoraCotizacion
import cl.duoc.tratohecho.utils.Validaciones
import androidx.compose.material.icons.filled.Lock
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ModalBottomSheet
import cl.duoc.tratohecho.ui.components.DialogoConfirmacion
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import cl.duoc.tratohecho.ui.components.AccionMenu
import cl.duoc.tratohecho.ui.components.MenuAcciones
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.BorderStroke
import cl.duoc.tratohecho.ui.theme.Pildora
import cl.duoc.tratohecho.ui.components.SeccionDescuento
import androidx.compose.material3.HorizontalDivider
import cl.duoc.tratohecho.ui.theme.DURACION_CORTA
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedContent
import androidx.compose.material3.Surface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.automirrored.filled.ShortText
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.tratohecho.data.local.entity.ProductoEntity
import cl.duoc.tratohecho.domain.model.Moneda
import cl.duoc.tratohecho.domain.model.TipoDescuento
import cl.duoc.tratohecho.ui.components.BarraConVolver
import cl.duoc.tratohecho.ui.components.BotonCarga
import cl.duoc.tratohecho.ui.components.CampoSelector
import cl.duoc.tratohecho.ui.components.CampoTexto
import cl.duoc.tratohecho.ui.components.FilaItem
import cl.duoc.tratohecho.ui.components.TarjetaTotales
import cl.duoc.tratohecho.ui.components.TituloSeccion
import cl.duoc.tratohecho.utils.Formatos
import cl.duoc.tratohecho.viewmodel.CotizacionFormViewModel
import cl.duoc.tratohecho.viewmodel.EventoFormCotizacion
import cl.duoc.tratohecho.viewmodel.ItemFormState

@Composable
fun CotizacionFormScreen(
    mostrarMensaje: (String) -> Unit,
    onVolver: () -> Unit,
    onIrADetalle: (id: String, abrirCorreo: Boolean) -> Unit,
    viewModel: CotizacionFormViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val clientes by viewModel.clientes.collectAsStateWithLifecycle()
    val productos by viewModel.productos.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                is EventoFormCotizacion.Guardada -> {
                    mostrarMensaje(evento.mensaje)
                    onVolver()
                }
                is EventoFormCotizacion.Enviada -> onIrADetalle(evento.id, true)
                is EventoFormCotizacion.EnCola -> {
                    mostrarMensaje(evento.mensaje)
                    onIrADetalle(evento.id, false)
                }
                is EventoFormCotizacion.Mensaje -> mostrarMensaje(evento.texto)
            }
        }
    }

    // Prevención de errores: salir con cambios sin guardar pide confirmación
    // (se compara contra lo que había al abrir el formulario)
    val firma = listOf(
        estado.clienteId, estado.contactoId, estado.moneda, estado.diasValidez, estado.notas,
        estado.items, estado.descuentoGlobal, estado.descuentoGlobalTipo
    )
    var firmaInicial by remember { mutableStateOf<List<Any?>?>(null) }
    LaunchedEffect(estado.cargando) { if (!estado.cargando && firmaInicial == null) firmaInicial = firma }
    val hayCambios = firmaInicial != null && firma != firmaInicial && !estado.guardando
    var confirmarSalida by remember { mutableStateOf(false) }
    val intentarSalir = { if (hayCambios) confirmarSalida = true else onVolver() }
    BackHandler(enabled = hayCambios) { confirmarSalida = true }

    val titulo = when {
        estado.esNueva -> "Nueva cotización"
        else -> "Editar ${estado.base?.numeroCompleto ?: ""}"
    }

    Scaffold(
        topBar = { BarraConVolver(titulo, intentarSalir) },
        // La acción principal queda siempre a mano, con el total al día
        bottomBar = {
            if (!estado.cargando) {
                BarraEnvio(
                    total = Formatos.monto(estado.totales.total, estado.moneda),
                    guardando = estado.guardando,
                    onEnviar = viewModel::guardarYEnviar,
                    onBorrador = viewModel::guardarBorrador
                )
            }
        }
    ) { padding ->
        if (estado.cargando) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        val clienteSeleccionado = clientes.find { it.cliente.id == estado.clienteId }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                // Sin imePadding aquí: la barra inferior ya sube con el teclado y el Scaffold
                // descuenta su altura; sumarlo de nuevo dejaba el formulario aplastado
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ---------- Cliente ----------
            CampoSelector(
                etiqueta = "Cliente",
                icono = Icons.Filled.Business,
                opciones = clientes,
                seleccionado = clienteSeleccionado,
                textoDe = { it.cliente.razonSocial },
                textoSecundarioDe = { "${it.cliente.rut} · ${it.contactos.size} contacto(s)" },
                onSeleccionar = viewModel::onClienteChange,
                error = estado.errores.cliente
            )
            AnimatedVisibility(visible = clienteSeleccionado != null && clienteSeleccionado.contactos.isNotEmpty()) {
                CampoSelector(
                    etiqueta = "Contacto (recibe el correo)",
                    icono = Icons.Filled.Person,
                    opciones = clienteSeleccionado?.contactos ?: emptyList(),
                    seleccionado = clienteSeleccionado?.contactos?.find { it.id == estado.contactoId },
                    textoDe = { it.nombre },
                    textoSecundarioDe = { "${it.cargo} · ${it.correo}" },
                    onSeleccionar = { viewModel.onContactoChange(it.id) }
                )
            }

            // ---------- Condiciones: moneda y validez en una sola fila ----------
            // La moneda es un desplegable compacto: casi siempre queda en CLP y no merece una fila entera
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                CampoMoneda(
                    moneda = estado.moneda,
                    onSeleccionar = viewModel::onMonedaChange,
                    habilitado = !estado.cargandoTipoCambio,
                    modifier = Modifier.weight(1f)
                )
                CampoTexto(
                    valor = estado.diasValidez,
                    onValorChange = viewModel::onDiasValidezChange,
                    etiqueta = "Validez (días)",
                    icono = Icons.Filled.EventAvailable,
                    error = estado.errores.diasValidez,
                    mostrarValido = estado.intentoGuardar,
                    tipoTeclado = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }
            Column(Modifier.animateContentSize()) {
                if (estado.cargandoTipoCambio) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                        Text("  Consultando el valor del día…", style = MaterialTheme.typography.bodySmall)
                    }
                } else if (estado.moneda != Moneda.CLP) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (estado.tipoCambioDesdeCache) {
                            Icon(Icons.Filled.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                        }
                        Text(
                            "1 ${estado.moneda.name} = ${Formatos.monto(estado.tipoCambio)} (${estado.fechaTipoCambio ?: "-"})" +
                                if (estado.tipoCambioDesdeCache) " · último valor guardado" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (estado.tipoCambioDesdeCache) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = viewModel::actualizarTipoCambio) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Actualizar valor")
                        }
                    }
                }
                estado.errores.tipoCambio?.let { error ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        Text("  $error", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // ---------- Ítems ----------
            Row(verticalAlignment = Alignment.CenterVertically) {
                TituloSeccion("Ítems (${estado.items.size})", modifier = Modifier.weight(1f))
                OutlinedButton(
                    onClick = viewModel::abrirNuevoItem,
                    shape = Pildora,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text("Agregar ítem")
                }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.animateContentSize().padding(vertical = 6.dp)
            ) {
                if (estado.items.isEmpty() && estado.errores.items == null) {
                    Text(
                        "Aún no agregas ítems. Elige productos del catálogo con \"Agregar ítem\".",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                estado.errores.items?.let { error ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        Text("  $error", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
                estado.items.forEach { item ->
                    // Tocar la fila edita el ítem; el menú ⋮ tiene editar y quitar
                    FilaItem(item, estado.moneda, onClick = { viewModel.editarItem(item) }) {
                        MenuAcciones(
                            listOf(
                                AccionMenu("Editar ítem", Icons.Outlined.Edit) { viewModel.editarItem(item) },
                                AccionMenu("Quitar de la cotización", Icons.Outlined.Delete, destructiva = true) { viewModel.eliminarItem(item) }
                            ),
                            descripcion = "Opciones de ${item.descripcion}"
                        )
                    }
                }
            }

            // ---------- Notas ----------
            CampoTexto(
                valor = estado.notas,
                onValorChange = viewModel::onNotasChange,
                etiqueta = "Notas (opcional)",
                icono = Icons.AutoMirrored.Filled.Notes,
                error = estado.errores.notas,
                lineas = 3,
                ayuda = "${estado.notas.length}/500"
            )

            TarjetaTotales(estado.totales, estado.moneda, estado.tipoCambio, Modifier.padding(top = 8.dp))
            // El descuento es opcional: queda como un enlace discreto bajo los totales
            SeccionDescuento(
                textoEnlace = "Agregar descuento a la cotización",
                tipo = estado.descuentoGlobalTipo,
                onTipoChange = viewModel::onDescuentoGlobalTipoChange,
                valor = estado.descuentoGlobal,
                onValorChange = viewModel::onDescuentoGlobalChange,
                error = estado.errores.descuentoGlobal,
                moneda = estado.moneda
            )
            Spacer(Modifier.height(8.dp))


        }
    }

    if (confirmarSalida) {
        DialogoConfirmacion(
            titulo = "¿Descartar los cambios?",
            mensaje = "Si sales ahora se pierde lo que no has guardado. Puedes guardarlo como borrador para terminarlo después.",
            textoConfirmar = "Descartar",
            textoCancelar = "Seguir editando",
            destructivo = true,
            onConfirmar = {
                confirmarSalida = false
                onVolver()
            },
            onCancelar = { confirmarSalida = false }
        )
    }

    // Sin señal: se puede seguir en USD/UF con el último valor guardado, pero se avisa
    estado.avisoMoneda?.let { aviso ->
        AlertDialog(
            onDismissRequest = viewModel::cerrarAvisoMoneda,
            icon = { Icon(Icons.Filled.CloudOff, contentDescription = null) },
            title = { Text("Sin conexión") },
            text = { Text(aviso) },
            confirmButton = { TextButton(onClick = viewModel::cerrarAvisoMoneda) { Text("Continuar") } }
        )
    }

    estado.dialogoItem?.let { dialogo ->
        DialogoItem(
            estado = dialogo,
            moneda = estado.moneda,
            productos = productos,
            onProducto = viewModel::onItemProductoChange,
            onDescripcion = viewModel::onItemDescripcionChange,
            onCantidad = viewModel::onItemCantidadChange,
            onDescuentoTipo = viewModel::onItemDescuentoTipoChange,
            onDescuento = viewModel::onItemDescuentoChange,
            onGuardar = viewModel::guardarItem,
            onCancelar = viewModel::cerrarItem
        )
    }
}

// Formulario de ítem: se elige un producto del catálogo (que trae su impuesto) y se ajusta cantidad, precio y descuento
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoItem(
    estado: ItemFormState,
    moneda: Moneda,
    productos: List<ProductoEntity>,
    onProducto: (ProductoEntity) -> Unit,
    onDescripcion: (String) -> Unit,
    onCantidad: (String) -> Unit,
    onDescuentoTipo: (TipoDescuento) -> Unit,
    onDescuento: (String) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit
) {
    // Hoja inferior en vez de diálogo: los campos tienen espacio y el teclado no tapa los botones
    ModalBottomSheet(
        onDismissRequest = onCancelar,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    if (estado.esNuevo) "Agregar ítem" else "Editar ítem",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                CampoSelector(
                    etiqueta = "Producto o servicio",
                    icono = Icons.Filled.Inventory2,
                    opciones = productos,
                    seleccionado = productos.find { it.id == estado.productoId },
                    textoDe = { it.nombre },
                    textoSecundarioDe = { "${Formatos.monto(it.precio)} · ${it.descripcion}" },
                    onSeleccionar = onProducto,
                    error = estado.errores.producto
                )
                CampoTexto(estado.descripcion, onDescripcion, "Descripción", Icons.AutoMirrored.Filled.ShortText, error = estado.errores.descripcion)
                CampoTexto(
                    estado.cantidad, onCantidad, "Cantidad ${estado.unidad}".trim(), Icons.Filled.Numbers,
                    error = estado.errores.cantidad, tipoTeclado = KeyboardType.Decimal
                )
                // Precio e impuesto los fija el administrador en el catálogo: se muestran, no se editan
                if (estado.productoId != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        // Cálculo en vivo: cambia apenas se edita la cantidad o el descuento
                        val cantidad = Validaciones.aNumero(estado.cantidad)
                        val precio = Validaciones.aNumero(estado.precio)
                        val bruto = cantidad * precio
                        val descuento = CalculadoraCotizacion.calcularDescuento(
                            bruto, estado.descuentoTipo, Validaciones.aNumero(estado.descuento)
                        )
                        val neto = bruto - descuento
                        val impuesto = neto * estado.impuestoPorcentaje / 100.0
                        val totalItem = CalculadoraCotizacion.redondear(neto + impuesto, moneda)

                        FilaCalculo("Precio unitario", Formatos.monto(precio, moneda))
                        FilaCalculo(
                            "Subtotal (${Formatos.numero(cantidad)} × ${Formatos.monto(precio, moneda)})",
                            Formatos.monto(bruto, moneda)
                        )
                        if (descuento > 0) FilaCalculo("Descuento", "− ${Formatos.monto(descuento, moneda)}")
                        if (estado.impuestoNombre != null) {
                            FilaCalculo(
                                if (estado.impuestoPorcentaje > 0) "${estado.impuestoNombre} ${Formatos.numero(estado.impuestoPorcentaje)}%"
                                else estado.impuestoNombre,
                                Formatos.monto(impuesto, moneda)
                            )
                        }
                        HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Total del ítem", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                            AnimatedContent(
                                targetState = Formatos.monto(totalItem, moneda),
                                transitionSpec = { fadeIn(tween(DURACION_CORTA)) togetherWith fadeOut(tween(120)) },
                                label = "total_item"
                            ) { texto ->
                                Text(texto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Precio e impuesto definidos en el catálogo",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                estado.errores.precio?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                estado.errores.impuesto?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                SeccionDescuento(
                    textoEnlace = "Agregar descuento al ítem",
                    tipo = estado.descuentoTipo,
                    onTipoChange = onDescuentoTipo,
                    valor = estado.descuento,
                    onValorChange = onDescuento,
                    error = estado.errores.descuento,
                    moneda = moneda
                )
                Spacer(Modifier.height(16.dp))
                BotonCarga(
                    texto = if (estado.esNuevo) "Agregar a la cotización" else "Guardar cambios",
                    onClick = onGuardar,
                    cargando = false
                )
                TextButton(onClick = onCancelar, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
    }
}

// Fila "concepto ........ monto" del cálculo del ítem
@Composable
private fun FilaCalculo(concepto: String, monto: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            concepto,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f).padding(end = 12.dp)
        )
        Text(monto, style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"))
    }
}

// Desplegable compacto de moneda: muestra el código (CLP, USD, UF) y al tocarlo
// abre un menú anclado al campo con el nombre completo de cada una
@Composable
private fun CampoMoneda(
    moneda: Moneda,
    onSeleccionar: (Moneda) -> Unit,
    habilitado: Boolean,
    modifier: Modifier = Modifier
) {
    var abierto by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedTextField(
            value = moneda.name,
            onValueChange = {},
            readOnly = true,
            enabled = habilitado,
            label = { Text("Moneda") },
            leadingIcon = { Icon(Icons.Filled.Payments, contentDescription = null) },
            trailingIcon = {
                Icon(
                    if (abierto) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null
                )
            },
            supportingText = {},
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier.fillMaxWidth()
        )
        // Capa que recibe el toque sobre todo el campo (el texto es de solo lectura)
        Box(
            Modifier
                .matchParentSize()
                .padding(bottom = 22.dp)
                .clip(MaterialTheme.shapes.medium)
                .clickable(enabled = habilitado, role = Role.DropdownList) { abierto = true }
        )
        DropdownMenu(
            expanded = abierto,
            onDismissRequest = { abierto = false },
            shape = MaterialTheme.shapes.medium,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ) {
            Moneda.entries.forEach { opcion ->
                val elegida = opcion == moneda
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                opcion.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (elegida) FontWeight.SemiBold else FontWeight.Normal
                            )
                            Text(
                                opcion.etiqueta,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    trailingIcon = { if (elegida) Icon(Icons.Filled.Check, contentDescription = "Seleccionada") },
                    onClick = {
                        abierto = false
                        if (!elegida) onSeleccionar(opcion)
                    }
                )
            }
        }
    }
}

// Barra inferior fija: total en vivo + "Guardar y enviar" (principal) + "Guardar borrador" (secundaria)
@Composable
private fun BarraEnvio(total: String, guardando: Boolean, onEnviar: () -> Unit, onBorrador: () -> Unit) {
    // Fondo un tono más gris y una línea arriba para separarla del formulario
    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Column(
            Modifier
                // Una sola vez: barra de navegación o teclado, el que sea más alto
                .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Total", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                AnimatedContent(
                    targetState = total,
                    transitionSpec = { fadeIn(tween(DURACION_CORTA)) togetherWith fadeOut(tween(120)) },
                    label = "total_barra"
                ) { texto ->
                    Text(texto, style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"))
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBorrador, enabled = !guardando) {
                    Text("Guardar borrador", color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(Modifier.width(8.dp))
                BotonCarga(
                    texto = "Guardar y enviar",
                    onClick = onEnviar,
                    cargando = guardando,
                    icono = Icons.AutoMirrored.Filled.Send,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
