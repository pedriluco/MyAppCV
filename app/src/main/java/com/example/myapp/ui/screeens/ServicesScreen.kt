package com.example.myapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.viewmodel.ServiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(
    tenantId: Long,
    onBack: () -> Unit,
    vm: ServiceViewModel = viewModel()
) {
    val state by vm.state.collectAsState()

    var name by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }

    LaunchedEffect(tenantId) {
        vm.load(tenantId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Servicios") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("<") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Text("Nuevo servicio", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it.filter { c -> c.isDigit() } },
                label = { Text("Duración (min)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    vm.create(
                        tenantId,
                        name,
                        duration.toIntOrNull() ?: 0
                    )
                    name = ""
                    duration = ""
                },
                enabled = !state.creating
            ) {
                Text(if (state.creating) "Creando..." else "Crear")
            }

            Spacer(Modifier.height(16.dp))

            Divider()
            Spacer(Modifier.height(12.dp))

            if (state.loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
            }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            LazyColumn {
                items(state.items) { s ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(s.name, style = MaterialTheme.typography.titleMedium)
                                Text("${s.durationMinutes} min")
                            }

                            Switch(
                                checked = s.active,
                                onCheckedChange = {
                                    vm.toggleActive(tenantId, s)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
