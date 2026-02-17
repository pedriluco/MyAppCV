package com.example.myapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapp.ui.AppCard
import com.example.myapp.ui.CenterLoading
import com.example.myapp.ui.CenterText
import com.example.myapp.ui.ScreenScaffold
import com.example.myapp.ui.Ui
import com.example.myapp.viewmodel.TenantViewModel
import androidx.compose.ui.unit.dp

@Composable
fun AdminRequestsScreen(
    navController: NavController,
    tenantVm: TenantViewModel = viewModel(),
    role: String
) {
    val state by tenantVm.uiState.collectAsState()

    LaunchedEffect(Unit) {
        tenantVm.loadPendingTenants()
    }

    val pending = state.tenants

    ScreenScaffold(
        title = "Solicitudes",
        canBack = true,
        onBack = { navController.popBackStack() },
        scroll = false,
        actions = {
            if (!state.isLoading) {
                TextButton(onClick = { tenantVm.loadPendingTenants() }) { Text("Recargar") }
            }
        }
    ) {
        when {
            state.isLoading -> CenterLoading()
            state.error != null -> CenterText(state.error ?: "Error")
            pending.isEmpty() -> CenterText("No hay negocios pendientes")
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(Ui.Gap)
                ) {
                    items(pending) { t ->
                        val id = t.id ?: 0L
                        AppCard {
                            Text(t.name, style = MaterialTheme.typography.titleMedium)
                            Text("Estado: PENDING", style = MaterialTheme.typography.bodySmall)

                            Spacer(Modifier.height(8.dp))

                            Button(
                                onClick = { tenantVm.approveTenant(id) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !state.isLoading && id != 0L
                            ) { Text("Aprobar") }
                        }
                    }
                }
            }
        }
    }
}
