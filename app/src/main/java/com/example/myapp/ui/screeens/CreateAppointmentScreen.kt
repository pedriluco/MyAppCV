package com.example.myapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.viewmodel.AppointmentViewModel // <- AJUSTA ESTE IMPORT A TU RUTA REAL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAppointmentScreen(
    tenantId: Long,
    onBack: () -> Unit,
    onGoToAgenda: (Long) -> Unit,
    appointmentViewModel: AppointmentViewModel = viewModel()
) {
    // ✅ si tu VM expone uiState como StateFlow/LiveData, esto lo resuelve
    // Si tu uiState NO es StateFlow, dime cómo lo tienes y lo adapto.
    val uiState by appointmentViewModel.uiState.collectAsState()

    var clientName by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("2026-01-15") }
    var time by remember { mutableStateOf("16:00") }
    var serviceIdText by remember { mutableStateOf("1") }

    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ errores como snackbar (opcional)
    LaunchedEffect(uiState.error) {
        uiState.error?.let { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Agendar cita") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = clientName,
                onValueChange = { clientName = it },
                label = { Text("Cliente") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Fecha (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = time,
                onValueChange = { time = it },
                label = { Text("Hora (HH:mm)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = serviceIdText,
                onValueChange = { serviceIdText = it },
                label = { Text("Service ID") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                onClick = {
                    val serviceId = serviceIdText.toLongOrNull() ?: 1L

                    if (clientName.isNotBlank() && date.isNotBlank() && time.isNotBlank()) {
                        appointmentViewModel.createAppointment(
                            tenantId = tenantId,
                            serviceId = serviceId,
                            clientName = clientName,
                            date = date,
                            time = time,
                            onSuccess = {
                                onGoToAgenda(tenantId) // ✅ aquí navega a agenda
                            }
                        )
                    }
                }
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Guardando…")
                } else {
                    Text("Guardar cita")
                }
            }
        }
    }
}
