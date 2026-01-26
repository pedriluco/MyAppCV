package com.example.myapp.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapp.network.ApiClient
import com.example.myapp.network.CreateAppointmentRequest
import com.example.myapp.ui.AppCard
import com.example.myapp.ui.ScreenScaffold
import com.example.myapp.ui.Ui
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

@Composable
fun PickDateTimeScreen(
    tenantId: Long,
    serviceId: Long,
    onBack: () -> Unit,
    onCreated: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var clientName by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf("") } // YYYY-MM-DD
    var timeText by remember { mutableStateOf("") } // HH:mm

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val cal = remember { Calendar.getInstance() }

    fun pickDate() {
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH)
        val d = cal.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(context, { _, yy, mm, dd ->
            dateText = String.format(Locale.US, "%04d-%02d-%02d", yy, mm + 1, dd)
        }, y, m, d).show()
    }

    fun pickTime() {
        val hh = cal.get(Calendar.HOUR_OF_DAY)
        val min = cal.get(Calendar.MINUTE)

        TimePickerDialog(context, { _, h, m ->
            timeText = String.format(Locale.US, "%02d:%02d", h, m)
        }, hh, min, true).show()
    }

    ScreenScaffold(
        title = "Agendar cita",
        canBack = true,
        onBack = onBack,
        scroll = false
    ) {
        AppCard {
            Text("Datos", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = clientName,
                onValueChange = { clientName = it },
                label = { Text("Tu nombre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { pickDate() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (dateText.isBlank()) "Elegir fecha" else dateText)
                }

                OutlinedButton(
                    onClick = { pickTime() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (timeText.isBlank()) "Elegir hora" else timeText)
                }
            }

            if (loading) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error ?: "Error", color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(Ui.Gap))

            Button(
                onClick = {
                    error = null

                    if (clientName.isBlank()) {
                        error = "Pon tu nombre"
                        return@Button
                    }
                    if (dateText.isBlank()) {
                        error = "Elige una fecha"
                        return@Button
                    }
                    if (timeText.isBlank()) {
                        error = "Elige una hora"
                        return@Button
                    }

                    loading = true
                    scope.launch {
                        try {
                            val req = CreateAppointmentRequest(
                                tenantId = tenantId,
                                serviceId = serviceId,
                                clientName = clientName.trim(),
                                date = dateText,
                                time = timeText
                            )

                            ApiClient.appointmentApi.create(tenantId, req)

                            onCreated()
                        } catch (e: Exception) {
                            error = e.message ?: "Error creando cita"
                        } finally {
                            loading = false
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirmar cita")
            }
        }
    }
}
