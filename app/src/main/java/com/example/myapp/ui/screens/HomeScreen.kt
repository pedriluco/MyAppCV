package com.example.myapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.viewmodel.AuthViewModel
import com.example.myapp.viewmodel.TenantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authVm: AuthViewModel,
    onLogout: () -> Unit,
    onGoToCreate: () -> Unit,
    onGoToAgenda: (Long) -> Unit,
    onGoToCreateAppointment: (Long) -> Unit,
    onGoToServices: (Long) -> Unit,
    onGoToHours: (Long) -> Unit,
    onGoToAdminRequests: () -> Unit,
    tenantViewModel: TenantViewModel = viewModel()
) {
    var query by remember { mutableStateOf("") }
    val uiState by tenantViewModel.uiState.collectAsState()

    val isAdmin = authVm.isAdmin()
    val isOwnerOrAdmin = authVm.isOwnerOrAdmin()

    LaunchedEffect(Unit) {
        tenantViewModel.loadAllTenants()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Negocios") },
                actions = {
                    if (isAdmin) {
                        TextButton(onClick = onGoToAdminRequests) { Text("Solicitudes") }
                        Spacer(Modifier.width(8.dp))
                    }

                    if (isOwnerOrAdmin) {
                        TextButton(onClick = onGoToCreate) { Text("Crear negocio") }
                        Spacer(Modifier.width(8.dp))
                    }

                    TextButton(onClick = {
                        authVm.logout()
                        onLogout()
                    }) {
                        Text("Salir")
                    }
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

            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    tenantViewModel.search(query)
                },
                label = { Text("Buscar por nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
            }

            uiState.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
            }

            if (!uiState.isLoading && uiState.tenants.isEmpty()) {
                Text("No se encontraron negocios.")
                return@Column
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(uiState.tenants) { tenant ->
                    val id = tenant.id

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(tenant.name, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { if (id != null) onGoToCreateAppointment(id) },
                                    enabled = id != null
                                ) { Text("Agendar") }

                                if (isOwnerOrAdmin) {
                                    OutlinedButton(
                                        onClick = { if (id != null) onGoToAgenda(id) },
                                        enabled = id != null
                                    ) { Text("Agenda") }

                                    OutlinedButton(
                                        onClick = { if (id != null) onGoToServices(id) },
                                        enabled = id != null
                                    ) { Text("Servicios") }

                                    OutlinedButton(
                                        onClick = { if (id != null) onGoToHours(id) },
                                        enabled = id != null
                                    ) { Text("Horarios") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
