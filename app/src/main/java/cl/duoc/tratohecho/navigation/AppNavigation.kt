package cl.duoc.tratohecho.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.duoc.tratohecho.ui.screens.login.LoginScreen
import cl.duoc.tratohecho.ui.screens.principal.PrincipalScreen
import cl.duoc.tratohecho.viewmodel.EstadoInicio
import cl.duoc.tratohecho.viewmodel.MainViewModel

// Navegación raíz: Login <-> Principal
@Composable
fun AppNavigation(
    destinoNotificacion: DestinoNotificacion?,
    onDestinoAtendido: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val estadoInicio by viewModel.estadoInicio.collectAsStateWithLifecycle()

    if (estadoInicio == EstadoInicio.Cargando) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val navController = rememberNavController()
    val inicio = if (estadoInicio == EstadoInicio.ConSesion) AppRoutes.PRINCIPAL else AppRoutes.LOGIN

    // Si la sesión se cierra (logout) se vuelve al login limpiando el historial
    LaunchedEffect(estadoInicio) {
        if (estadoInicio == EstadoInicio.SinSesion && navController.currentDestination?.route != AppRoutes.LOGIN) {
            navController.navigate(AppRoutes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = inicio) {
        composable(AppRoutes.LOGIN) {
            LoginScreen(
                onIngresoExitoso = {
                    navController.navigate(AppRoutes.PRINCIPAL) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(AppRoutes.PRINCIPAL) {
            PrincipalScreen(
                destinoNotificacion = destinoNotificacion,
                onDestinoAtendido = onDestinoAtendido
            )
        }
    }
}
