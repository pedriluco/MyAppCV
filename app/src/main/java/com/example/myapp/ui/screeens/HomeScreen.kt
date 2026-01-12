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
    tenantViewModel: TenantViewModel = viewModel()
) {
    var query by remember { mutableStateOf("") }
    val uiState by tenantViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        tenantViewModel.loadAllTenants()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buscar negocios") },
                actions = {
                    if (authVm.isOwnerOrAdmin()) {
                        TextButton(onClick = onGoToCreate) { Text("Crear") }
                        Spacer(Modifier.width(8.dp))
                    }
                    Button(onClick = {
                        authVm.logout()
                        onLogout()
                    }) {
                        Text("Logout")
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
                label = { Text("Busca por nombre") },
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

            Text("Resultados", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (!uiState.isLoading && uiState.tenants.isEmpty()) {
                Text("No se encontraron negocios.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(uiState.tenants) { tenant ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(tenant.name)

                                Row {
                                    if (authVm.isOwnerOrAdmin()) {
                                        TextButton(
                                            onClick = {
                                                val id = tenant.id ?: return@TextButton
                                                onGoToAgenda(id)
                                            }
                                        ) {
                                            Text("Agenda")
                                        }
                                        Spacer(Modifier.width(8.dp))
                                    }

                                    TextButton(
                                        onClick = {
                                            val id = tenant.id ?: return@TextButton
                                            onGoToCreateAppointment(id)
                                        }
                                    ) {
                                        Text("Agendar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
