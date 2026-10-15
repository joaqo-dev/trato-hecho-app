package cl.duoc.tratohecho.ui.screens.principal

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.dp
import cl.duoc.tratohecho.ui.components.BarraNavegacionFlotante
import cl.duoc.tratohecho.ui.components.ESPACIO_BARRA_NAVEGACION
import cl.duoc.tratohecho.ui.theme.DURACION_CORTA
import cl.duoc.tratohecho.ui.theme.DURACION_MEDIA
import cl.duoc.tratohecho.ui.theme.EaseOutFuerte
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.tratohecho.navigation.AppRoutes
import cl.duoc.tratohecho.navigation.DestinoNotificacion
import cl.duoc.tratohecho.ui.components.BannerSinConexion
import cl.duoc.tratohecho.ui.components.BannerSincronizando
import cl.duoc.tratohecho.ui.screens.ajustes.AjustesScreen
import cl.duoc.tratohecho.ui.screens.ajustes.EmpresaFormScreen
import cl.duoc.tratohecho.ui.screens.ajustes.UsuarioFormScreen
import cl.duoc.tratohecho.ui.screens.ajustes.UsuariosScreen
import cl.duoc.tratohecho.ui.screens.catalogo.CatalogoScreen
import cl.duoc.tratohecho.ui.screens.catalogo.ImpuestoFormScreen
import cl.duoc.tratohecho.ui.screens.catalogo.ProductoFormScreen
import cl.duoc.tratohecho.ui.screens.catalogo.UnidadFormScreen
import cl.duoc.tratohecho.ui.screens.clientes.ClienteDetalleScreen
import cl.duoc.tratohecho.ui.screens.clientes.ClienteFormScreen
import cl.duoc.tratohecho.ui.screens.clientes.ClientesScreen
import cl.duoc.tratohecho.ui.screens.cotizaciones.CotizacionDetalleScreen
import cl.duoc.tratohecho.ui.screens.cotizaciones.CotizacionFormScreen
import cl.duoc.tratohecho.ui.screens.cotizaciones.CotizacionesScreen
import cl.duoc.tratohecho.ui.screens.inicio.InicioScreen
import cl.duoc.tratohecho.viewmodel.PrincipalViewModel
import kotlinx.coroutines.launch

// Contenedor principal: barra inferior por rol + navegación anidada de todas las secciones
@Composable
fun PrincipalScreen(
    destinoNotificacion: DestinoNotificacion?,
    onDestinoAtendido: () -> Unit,
    viewModel: PrincipalViewModel = hiltViewModel()
) {
    val sesion by viewModel.sesion.collectAsStateWithLifecycle()
    val hayConexion by viewModel.hayConexion.collectAsStateWithLifecycle()
    val pendientes by viewModel.pendientes.collectAsStateWithLifecycle()
    val sincronizando by viewModel.sincronizando.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val mostrarMensaje: (String) -> Unit = { mensaje -> scope.launch { snackbar.showSnackbar(mensaje) } }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.mensajes.collect { snackbar.showSnackbar(it) }
    }

    // Recurso nativo: permiso de notificaciones en tiempo de ejecución (Android 13+)
    val permisoNotificaciones = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (!concedido) mostrarMensaje("Sin permiso de notificaciones no podremos avisarte cuando un cliente responda")
    }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            permisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val sesionActual = sesion
    if (sesionActual == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    // Al tocar una notificación se abre la cotización correspondiente
    LaunchedEffect(destinoNotificacion) {
        destinoNotificacion?.let {
            navController.navigate(AppRoutes.cotizacionDetalle(it.cotizacionId, it.abrirCorreo))
            onDestinoAtendido()
        }
    }

    val secciones = seccionesPara(sesionActual.rol)
    val entradaActual by navController.currentBackStackEntryAsState()
    val destinoActual = entradaActual?.destination
    val mostrarBarra = secciones.any { it.ruta == destinoActual?.route }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        containerColor = MaterialTheme.colorScheme.background,
        // El aviso (snackbar) aparece sobre la barra flotante, no detrás de ella
        snackbarHost = {
            SnackbarHost(snackbar, Modifier.padding(bottom = if (mostrarBarra) ESPACIO_BARRA_NAVEGACION else 0.dp))
        },
        topBar = {
            Column(
                Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
            ) {
                BannerSinConexion(sinConexion = !hayConexion, pendientes = pendientes)
                BannerSincronizando(visible = sincronizando && hayConexion)
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = AppRoutes.INICIO,
            enterTransition = { fadeIn(tween(DURACION_MEDIA)) + slideInHorizontally(tween(DURACION_MEDIA, easing = EaseOutFuerte)) { it / 8 } },
            exitTransition = { fadeOut(tween(DURACION_CORTA)) },
            popEnterTransition = { fadeIn(tween(DURACION_MEDIA)) },
            popExitTransition = { fadeOut(tween(DURACION_CORTA)) + slideOutHorizontally(tween(DURACION_CORTA)) { it / 8 } },
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
        ) {
            // ---------- Secciones principales ----------
            composable(AppRoutes.INICIO) {
                // Las secciones de la barra se abren igual que desde la barra (sin apilar pantallas)
                val irASeccion: (String) -> Unit = { ruta ->
                    navController.navigate(ruta) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                InicioScreen(
                    sesion = sesionActual,
                    onSincronizar = viewModel::sincronizar,
                    onCerrarSesion = viewModel::cerrarSesion,
                    onNuevaCotizacion = { navController.navigate(AppRoutes.cotizacionForm()) },
                    onNuevoCliente = { navController.navigate(AppRoutes.clienteForm()) },
                    onAbrirCotizacion = { navController.navigate(AppRoutes.cotizacionDetalle(it)) },
                    onAbrirCotizaciones = { irASeccion(AppRoutes.COTIZACIONES) },
                    onAbrirClientes = { irASeccion(AppRoutes.CLIENTES) }
                )
            }
            composable(AppRoutes.COTIZACIONES) {
                CotizacionesScreen(
                    sesion = sesionActual,
                    onAbrir = { navController.navigate(AppRoutes.cotizacionDetalle(it)) },
                    onNueva = { navController.navigate(AppRoutes.cotizacionForm()) }
                )
            }
            composable(AppRoutes.CLIENTES) {
                ClientesScreen(
                    sesion = sesionActual,
                    onAbrir = { navController.navigate(AppRoutes.clienteDetalle(it)) },
                    onNuevo = { navController.navigate(AppRoutes.clienteForm()) }
                )
            }
            composable(AppRoutes.CATALOGO) {
                CatalogoScreen(
                    sesion = sesionActual,
                    mostrarMensaje = mostrarMensaje,
                    onNuevoProducto = { navController.navigate(AppRoutes.productoForm()) },
                    onEditarProducto = { navController.navigate(AppRoutes.productoForm(it)) },
                    onNuevaUnidad = { navController.navigate(AppRoutes.unidadForm()) },
                    onEditarUnidad = { navController.navigate(AppRoutes.unidadForm(it)) },
                    onNuevoImpuesto = { navController.navigate(AppRoutes.impuestoForm()) },
                    onEditarImpuesto = { navController.navigate(AppRoutes.impuestoForm(it)) }
                )
            }
            composable(AppRoutes.AJUSTES) {
                AjustesScreen(
                    sesion = sesionActual,
                    onUsuarios = { navController.navigate(AppRoutes.USUARIOS) },
                    onEmpresa = { navController.navigate(AppRoutes.EMPRESA_FORM) },
                    onCerrarSesion = viewModel::cerrarSesion
                )
            }

            // ---------- Cotizaciones ----------
            composable(
                AppRoutes.COTIZACION_DETALLE,
                arguments = listOf(
                    navArgument("id") { type = NavType.StringType },
                    navArgument("abrirCorreo") { type = NavType.BoolType; defaultValue = false }
                )
            ) {
                CotizacionDetalleScreen(
                    sesion = sesionActual,
                    mostrarMensaje = mostrarMensaje,
                    onVolver = { navController.popBackStack() },
                    onEditar = { navController.navigate(AppRoutes.cotizacionForm(it)) },
                    onAbrirCotizacion = { id ->
                        navController.navigate(AppRoutes.cotizacionDetalle(id)) {
                            popUpTo(AppRoutes.COTIZACION_DETALLE) { inclusive = true }
                        }
                    }
                )
            }
            composable(
                AppRoutes.COTIZACION_FORM,
                arguments = listOf(navArgument("id") { type = NavType.StringType; nullable = true; defaultValue = null })
            ) {
                CotizacionFormScreen(
                    mostrarMensaje = mostrarMensaje,
                    onVolver = { navController.popBackStack() },
                    onIrADetalle = { id, abrirCorreo ->
                        // Se reemplaza el formulario (y el detalle anterior, si venía de uno) por el detalle actualizado
                        navController.popBackStack()
                        if (navController.currentDestination?.route == AppRoutes.COTIZACION_DETALLE) {
                            navController.popBackStack()
                        }
                        navController.navigate(AppRoutes.cotizacionDetalle(id, abrirCorreo))
                    }
                )
            }

            // ---------- Clientes ----------
            composable(AppRoutes.CLIENTE_DETALLE, arguments = listOf(navArgument("id") { type = NavType.StringType })) {
                ClienteDetalleScreen(
                    sesion = sesionActual,
                    mostrarMensaje = mostrarMensaje,
                    onVolver = { navController.popBackStack() },
                    onEditar = { navController.navigate(AppRoutes.clienteForm(it)) },
                    onAbrirCotizacion = { navController.navigate(AppRoutes.cotizacionDetalle(it)) }
                )
            }
            composable(
                AppRoutes.CLIENTE_FORM,
                arguments = listOf(navArgument("id") { type = NavType.StringType; nullable = true; defaultValue = null })
            ) {
                ClienteFormScreen(mostrarMensaje = mostrarMensaje, onVolver = { navController.popBackStack() })
            }

            // ---------- Catálogo ----------
            composable(
                AppRoutes.PRODUCTO_FORM,
                arguments = listOf(navArgument("id") { type = NavType.StringType; nullable = true; defaultValue = null })
            ) {
                ProductoFormScreen(mostrarMensaje = mostrarMensaje, onVolver = { navController.popBackStack() })
            }
            composable(
                AppRoutes.UNIDAD_FORM,
                arguments = listOf(navArgument("id") { type = NavType.StringType; nullable = true; defaultValue = null })
            ) {
                UnidadFormScreen(mostrarMensaje = mostrarMensaje, onVolver = { navController.popBackStack() })
            }
            composable(
                AppRoutes.IMPUESTO_FORM,
                arguments = listOf(navArgument("id") { type = NavType.StringType; nullable = true; defaultValue = null })
            ) {
                ImpuestoFormScreen(mostrarMensaje = mostrarMensaje, onVolver = { navController.popBackStack() })
            }

            // ---------- Administración ----------
            composable(AppRoutes.USUARIOS) {
                UsuariosScreen(
                    sesion = sesionActual,
                    mostrarMensaje = mostrarMensaje,
                    onVolver = { navController.popBackStack() },
                    onNuevo = { navController.navigate(AppRoutes.usuarioForm()) },
                    onEditar = { navController.navigate(AppRoutes.usuarioForm(it)) }
                )
            }
            composable(
                AppRoutes.USUARIO_FORM,
                arguments = listOf(navArgument("id") { type = NavType.StringType; nullable = true; defaultValue = null })
            ) {
                UsuarioFormScreen(mostrarMensaje = mostrarMensaje, onVolver = { navController.popBackStack() })
            }
            composable(AppRoutes.EMPRESA_FORM) {
                EmpresaFormScreen(mostrarMensaje = mostrarMensaje, onVolver = { navController.popBackStack() })
            }
        }

        // Barra flotante superpuesta: el contenido pasa por debajo de la píldora.
        // Se oculta en detalles y formularios para dar todo el espacio al contenido.
        AnimatedVisibility(
            visible = mostrarBarra,
            enter = slideInVertically(tween(DURACION_MEDIA, easing = EaseOutFuerte)) { it } + fadeIn(tween(DURACION_CORTA)),
            exit = slideOutVertically(tween(DURACION_CORTA)) { it } + fadeOut(tween(DURACION_CORTA)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(top = 6.dp, bottom = 12.dp)
            ) {
                BarraNavegacionFlotante(
                    destinos = secciones,
                    rutaActual = secciones.firstOrNull { s -> destinoActual?.hierarchy?.any { it.route == s.ruta } == true }?.ruta,
                    onSeleccionar = { seccion ->
                        navController.navigate(seccion.ruta) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
        }
    }
}
