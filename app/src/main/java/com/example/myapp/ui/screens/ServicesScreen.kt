package com.example.myapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapp.network.ServiceDto
import com.example.myapp.ui.AppCard
import com.example.myapp.ui.CenterLoading
import com.example.myapp.ui.CenterText
import com.example.myapp.ui.ScreenScaffold
import com.example.myapp.ui.Ui
import com.example.myapp.viewmodel.ServiceViewModel



@Composable
fun ServicesScreen(
    navController: NavController,
    tenantId: Long,
    vm: ServiceViewModel = viewModel()
) {
    val state by vm.state.collectAsState()

    // carga inicial
    LaunchedEffect(tenantId) {
        vm.load(tenantId)
    }

    var showCreate by remember { mutableStateOf(false) }

    ScreenScaffold(
        title = "Servicios",
        canBack = true,
        onBack = { navController.popBackStack() },
        scroll = false,
        actions = {
            TextButton(onClick = { vm.load(tenantId) }) { Text("Recargar") }
            TextButton(onClick = { showCreate = true }) { Text("Agregar") }
        }
    ) {
        if (showCreate) {
            CreateServiceCard(
                creating = state.creating,
                onCancel = { showCreate = false },
                onCreate = { name, duration ->
                    vm.create(tenantId, name, duration)
                    // si se quedó "creating" true, igual se cierra para UI limpia
                    showCreate = false
                }
            )
        }

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
                CenterText("No hay servicios")
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(Ui.Gap)
                ) {
                    items(state.items) { s ->
                        ServiceItemCard(
                            service = s,
                            onToggle = { vm.toggleActive(tenantId, s) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceItemCard(
    service: ServiceDto,
    onToggle: () -> Unit
) {
    AppCard {
        Text(service.name, style = MaterialTheme.typography.titleMedium)
        Text("Duración: ${service.durationMinutes} min", style = MaterialTheme.typography.bodyMedium)
        Text(
            text = if (service.active) "Activo" else "Inactivo",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onToggle,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (service.active) "Desactivar" else "Activar")
            }
        }
    }
}

@Composable
private fun CreateServiceCard(
    creating: Boolean,
    onCancel: () -> Unit,
    onCreate: (String, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var durationText by remember { mutableStateOf("") }

    AppCard {
        Text("Nuevo servicio", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = durationText,
            onValueChange = { durationText = it.filter { ch -> ch.isDigit() } },
            label = { Text("Duración (min)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        if (creating) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                enabled = !creating,
                modifier = Modifier.weight(1f)
            ) { Text("Cancelar") }

            Button(
                onClick = {
                    val duration = durationText.toIntOrNull() ?: 0
                    onCreate(name, duration)
                },
                enabled = !creating,
                modifier = Modifier.weight(1f)
            ) { Text("Crear") }
        }
    }
}

