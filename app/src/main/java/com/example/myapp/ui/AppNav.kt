package com.example.myapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapp.data.TokenStore
import com.example.myapp.data.repository.AuthRepository
import com.example.myapp.ui.screens.*
import com.example.myapp.viewmodel.AuthViewModel
import com.example.myapp.viewmodel.AuthViewModelFactory
import com.example.myapp.viewmodel.TenantViewModel
import kotlinx.coroutines.launch

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val CREATE_BUSINESS = "create_business"

    const val CREATE_APPOINTMENT = "create_appointment/{tenantId}"
    const val AGENDA = "agenda/{tenantId}"
    const val SERVICES = "services/{tenantId}"
    const val HOURS = "hours/{tenantId}"

    fun createAppointment(tenantId: Long) = "create_appointment/$tenantId"
    fun agenda(tenantId: Long) = "agenda/$tenantId"
    fun services(tenantId: Long) = "services/$tenantId"
    fun hours(tenantId: Long) = "hours/$tenantId"
}

@Composable
fun AppNav(
    tokenStore: TokenStore
) {
    val nav = rememberNavController()
    val scope = rememberCoroutineScope()

    val context = LocalContext.current

    val authVm: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(
            AuthRepository(
                TokenStore(context)
            )
        )
    )

    val tenantVm: TenantViewModel = viewModel()

    val authState by authVm.state.collectAsState()

    LaunchedEffect(Unit) {
        authVm.loadToken()
    }

    LaunchedEffect(authState.checkedToken, authState.loggedIn) {
        if (authState.checkedToken && authState.loggedIn) {
            nav.navigate(Routes.HOME) {
                popUpTo(Routes.LOGIN) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = nav,
        startDestination = Routes.LOGIN
    ) {

        composable(Routes.LOGIN) {
            LoginScreen(
                vm = authVm,
                onLoggedIn = {
                    nav.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                authVm = authVm,
                tenantViewModel = tenantVm,
                onLogout = {
                    scope.launch {
                        authVm.logout()
                        nav.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                onGoToCreate = { nav.navigate(Routes.CREATE_BUSINESS) },
                onGoToAgenda = { tenantId -> nav.navigate(Routes.agenda(tenantId)) },
                onGoToCreateAppointment = { tenantId ->
                    nav.navigate(Routes.createAppointment(tenantId))
                },
                onGoToServices = { tenantId ->
                    nav.navigate(Routes.services(tenantId))
                },
                onGoToHours = { tenantId ->
                    nav.navigate(Routes.hours(tenantId))
                },
                onGoToAdminRequests = {
                    // luego lo conectamos
                }
            )
        }


        composable(Routes.CREATE_BUSINESS) {
            CreateBusinessScreen(
                onBack = { nav.popBackStack() },
                tenantViewModel = tenantVm
            )
        }

        composable(
            route = Routes.CREATE_APPOINTMENT,
            arguments = listOf(navArgument("tenantId") { type = NavType.LongType })
        ) { backStackEntry ->
            val tenantId = backStackEntry.arguments?.getLong("tenantId") ?: 0L

            CreateAppointmentScreen(
                tenantId = tenantId,
                onBack = { nav.popBackStack() },
                onGoToAgenda = { id ->
                    nav.navigate(Routes.agenda(id)) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Routes.AGENDA,
            arguments = listOf(navArgument("tenantId") { type = NavType.LongType })
        ) { backStackEntry ->
            val tenantId = backStackEntry.arguments?.getLong("tenantId") ?: 0L
            AgendaScreen(
                navController = nav,
                tenantId = tenantId
            )
        }

        composable(
            route = Routes.SERVICES,
            arguments = listOf(navArgument("tenantId") { type = NavType.LongType })
        ) { backStackEntry ->
            val tenantId = backStackEntry.arguments?.getLong("tenantId") ?: 0L
            ServicesScreen(
                navController = nav,
                tenantId = tenantId
            )
        }

        composable(
            route = Routes.HOURS,
            arguments = listOf(navArgument("tenantId") { type = NavType.LongType })
        ) { backStackEntry ->
            val tenantId = backStackEntry.arguments?.getLong("tenantId") ?: 0L
            BusinessHoursScreen(
                navController = nav,
                tenantId = tenantId
            )
        }
    }
}
