package com.example.myapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.viewmodel.AppointmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAppointmentScreen(
    tenantId: Long,
    onBack: () -> Unit,
    onGoToAgenda: (Long) -> Unit,
    appointmentViewModel: AppointmentViewModel = viewModel()
) {
    val uiState by appointmentViewModel.uiState.collectAsState()

    var clientName by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }     // YYYY-MM-DD
    var time by remember { mutableStateOf("") }     // HH:mm
    var serviceIdText by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Agendar cita") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Fecha (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = time,
                onValueChange = { time = it },
                label = { Text("Hora (HH:mm)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = serviceIdText,
                onValueChange = { serviceIdText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Service ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            val canSubmit =
                clientName.isNotBlank() &&
                        date.isNotBlank() &&
                        time.isNotBlank() &&
                        serviceIdText.toLongOrNull() != null &&
                        !uiState.isLoading

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = canSubmit,
                onClick = {
                    val serviceId = serviceIdText.toLongOrNull() ?: return@Button

                    appointmentViewModel.createAppointment(
                        tenantId = tenantId,
                        serviceId = serviceId,
                        clientName = clientName.trim(),
                        date = date.trim(),
                        time = time.trim(),
                        onSuccess = { onGoToAgenda(tenantId) }
                    )
                }
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Guardando…")
                } else {
                    Text("Guardar cita")
                }
            }
        }
    }
}
