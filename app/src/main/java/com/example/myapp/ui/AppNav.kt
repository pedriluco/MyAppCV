package com.example.myapp.ui

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapp.data.TokenStore
import com.example.myapp.data.repository.AuthRepository
import com.example.myapp.ui.screens.*
import com.example.myapp.viewmodel.AuthViewModel

@Composable
fun AppNav() {
    val context = LocalContext.current
    val tokenStore = remember { TokenStore(context) }
    val authRepo = remember { AuthRepository(tokenStore) }
    val authVm = remember { AuthViewModel(authRepo) }

    // cargar token una vez
    LaunchedEffect(Unit) {
        authVm.loadToken()
    }

    val state by authVm.state.collectAsState()
    val nav = rememberNavController()

    // mientras no sabemos si hay token, no arrancamos navegación
    if (!state.checkedToken) return

    val start = if (state.loggedIn) "home" else "login"
    val isOwnerOrAdmin = state.role == "OWNER" || state.role == "ADMIN"

    NavHost(
        navController = nav,
        startDestination = start
    ) {

        composable("login") {
            LoginScreen(
                vm = authVm,
                onLoggedIn = {
                    nav.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
                authVm = authVm,
                onLogout = {
                    authVm.logout()
                    nav.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onGoToCreate = { nav.navigate("create") },
                onGoToAgenda = { tenantId -> nav.navigate("agenda/$tenantId") },
                onGoToCreateAppointment = { tenantId -> nav.navigate("createAppointment/$tenantId") }
            )
        }

        // Crear negocio (si quieres, también puedes ocultarlo desde HomeScreen)
        composable("create") {
            CreateBusinessScreen(
                authVm = authVm,
                onBack = { nav.popBackStack() }
            )
        }

        // Crear cita (USER)
        composable(
            route = "createAppointment/{tenantId}",
            arguments = listOf(navArgument("tenantId") { type = NavType.LongType })
        ) { backStackEntry ->
            val tenantId = backStackEntry.arguments?.getLong("tenantId") ?: 1L

            CreateAppointmentScreen(
                tenantId = tenantId,
                authVm = authVm,
                onBack = { nav.popBackStack() },
                onGoToAgenda = { id ->
                    nav.navigate("agenda/$id") {
                        popUpTo("home")
                    }
                }
            )
        }

        // Agenda (OWNER/ADMIN debería verla, pero si entras como USER backend te frenará igual)
        composable(
            route = "agenda/{tenantId}",
            arguments = listOf(navArgument("tenantId") { type = NavType.LongType })
        ) { backStackEntry ->
            val tenantId = backStackEntry.arguments?.getLong("tenantId") ?: 1L

            AgendaScreen(
                tenantId = tenantId,
                onBack = { nav.popBackStack() }
            )
        }

        // ✅ RUTAS SOLO PARA OWNER/ADMIN
        if (isOwnerOrAdmin) {

            composable(
                route = "services/{tenantId}",
                arguments = listOf(navArgument("tenantId") { type = NavType.LongType })
            ) { backStackEntry ->
                val tenantId = backStackEntry.arguments?.getLong("tenantId") ?: 1L

                ServicesScreen(
                    tenantId = tenantId,
                    onBack = { nav.popBackStack() }
                )
            }

            composable(
                route = "hours/{tenantId}",
                arguments = listOf(navArgument("tenantId") { type = NavType.LongType })
            ) { backStackEntry ->
                val tenantId = backStackEntry.arguments?.getLong("tenantId") ?: 1L

                BusinessHoursScreen(
                    tenantId = tenantId,
                    onBack = { nav.popBackStack() }
                )
            }
        }
    }
}
