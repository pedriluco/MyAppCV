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
import com.example.myapp.viewmodel.AgendaViewModel
import androidx.compose.ui.unit.dp


@Composable
fun AgendaScreen(
    navController: NavController,
    tenantId: Long,
    vm: AgendaViewModel = viewModel()
) {
    val state by vm.state.collectAsState()

    // carga inicial
    LaunchedEffect(tenantId) {
        vm.load(tenantId)
    }

    ScreenScaffold(
        title = "Agenda",
        canBack = true,
        onBack = { navController.popBackStack() },
        scroll = false,
        actions = {
            if (!state.loading) {
                TextButton(onClick = { vm.load(tenantId) }) { Text("Recargar") }
            }
        }
    ) {
        when {
            state.loading -> {
                CenterLoading()
            }

            state.error != null -> {
                AppCard {
                    Text("Error", style = MaterialTheme.typography.titleMedium)
                    Text(state.error ?: "Error", style = MaterialTheme.typography.bodyMedium)

                    Spacer(Modifier.height(Ui.Gap))

                    Button(
                        onClick = { vm.load(tenantId) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Reintentar") }
                }
            }

            state.items.isEmpty() -> {
                CenterText("No hay citas todavía")
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(Ui.Gap)
                ) {
                    items(state.items) { a ->
                        AppCard {
                            Text(
                                text = a.clientName,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${a.startAt}  →  ${a.endAt}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Estado: ${a.status}",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Spacer(Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { vm.reject(tenantId, a.id) },
                                    enabled = !state.saving,
                                    modifier = Modifier.weight(1f)
                                ) { Text("Rechazar") }

                                Button(
                                    onClick = { vm.approve(tenantId, a.id) },
                                    enabled = !state.saving,
                                    modifier = Modifier.weight(1f)
                                ) { Text("Aprobar") }
                            }

                            if (state.saving) {
                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            }
        }
    }
}
