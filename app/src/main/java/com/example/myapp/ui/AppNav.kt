package com.example.myapp.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapp.ui.screens.AgendaScreen
import com.example.myapp.ui.screens.CreateAppointmentScreen
import com.example.myapp.ui.screens.CreateBusinessScreen
import com.example.myapp.ui.screens.HomeScreen

@Composable
fun AppNav() {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = "home"
    ) {

        composable("home") {
            HomeScreen(
                onGoToCreate = { nav.navigate("create") },
                onGoToAgenda = { tenantId ->
                    nav.navigate("agenda/$tenantId")
                },
                onGoToCreateAppointment = { tenantId ->
                    nav.navigate("createAppointment/$tenantId")
                }
            )
        }

        composable("create") {
            CreateBusinessScreen(
                onBack = { nav.popBackStack() }
            )
        }

        composable(
            route = "createAppointment/{tenantId}",
            arguments = listOf(
                navArgument("tenantId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val tenantId = backStackEntry.arguments?.getLong("tenantId") ?: 1L

            CreateAppointmentScreen(
                tenantId = tenantId,
                onBack = { nav.popBackStack() },
                onGoToAgenda = { id ->
                    nav.navigate("agenda/$id") {
                        popUpTo("home")
                    }
                }
            )
        }

        composable(
            route = "agenda/{tenantId}",
            arguments = listOf(
                navArgument("tenantId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val tenantId = backStackEntry.arguments?.getLong("tenantId") ?: 1L

            AgendaScreen(
                tenantId = tenantId,
                onBack = { nav.popBackStack() }
            )
        }
    }
}
