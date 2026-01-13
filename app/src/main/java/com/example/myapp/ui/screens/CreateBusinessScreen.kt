package com.example.myapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.viewmodel.TenantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBusinessScreen(
    onBack: () -> Unit,
    tenantViewModel: TenantViewModel = viewModel()
) {
    var businessName by remember { mutableStateOf("") }
    val uiState by tenantViewModel.uiState.collectAsState()

    // Si se creó OK, regresa a Home
    LaunchedEffect(uiState.justCreated) {
        if (uiState.justCreated) {
            businessName = ""
            tenantViewModel.resetJustCreated()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear negocio") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("← Buscar") }
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
                value = businessName,
                onValueChange = { businessName = it },
                label = { Text("Nombre del negocio") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    if (businessName.isNotBlank()) {
                        tenantViewModel.createTenant(businessName)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                Text(if (uiState.isLoading) "Creando..." else "Crear")
            }

            uiState.error?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
