package com.example.myapp.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapp.ui.AppCard
import com.example.myapp.ui.CenterLoading
import com.example.myapp.ui.ScreenScaffold
import com.example.myapp.ui.Ui
import com.example.myapp.viewmodel.CreateAppointmentViewModel
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAppointmentScreen(
    tenantId: Long,
    tenantStatus: String,
    navController: NavController,
    vm: CreateAppointmentViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    val isActive = tenantStatus == "ACTIVE"
    var clientName by remember { mutableStateOf("") }
    val cal = remember { Calendar.getInstance() }

    LaunchedEffect(tenantId, isActive) {
        if (isActive) {
            vm.loadServices(tenantId)
            vm.loadHours(tenantId)
        }
    }

    fun pickDate() {
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH)
        val d = cal.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            context,
            { _, yy, mm, dd ->
                vm.setDate(String.format(Locale.US, "%04d-%02d-%02d", yy, mm + 1, dd))
            },
            y, m, d
        ).apply {
            datePicker.minDate = System.currentTimeMillis() - 1000
        }.show()
    }

    fun pickTime() {
        if (state.selectedService == null || state.date.isBlank()) return

        val limits = vm.allowedTimeWindowMinutes()
        if (limits == null) {
            vm.setTime("")
            return
        }

        val (minStart, maxStart) = limits

        val current = state.time.takeIf { it.isNotBlank() }?.let { t ->
            val parts = t.split(":")
            if (parts.size == 2) (parts[0].toInt() * 60 + parts[1].toInt()) else null
        }

        val start = when {
            current == null -> minStart
            current < minStart -> minStart
            current > maxStart -> maxStart
            else -> current
        }

        val hh = start / 60
        val mm = start % 60

        TimePickerDialog(
            context,
            { _, h, m ->
                val candidate = String.format(Locale.US, "%02d:%02d", h, m)
                vm.setTime(candidate)
            },
            hh,
            mm,
            true
        ).show()
    }

    val endPreview = vm.computeEndTimePreview()
    val isPast = vm.isSelectedDateTimeInPast()

    val canSubmit =
        isActive &&
                state.canSubmit &&
                clientName.isNotBlank() &&
                !isPast &&
                !state.loading

    ScreenScaffold(
        title = "Crear cita",
        canBack = true,
        onBack = { navController.popBackStack() },
        scroll = false,
        actions = {
            if (isActive && !state.loading) {
                TextButton(onClick = { vm.loadServices(tenantId) }) { Text("Servicios") }
                TextButton(onClick = { vm.loadHours(tenantId) }) { Text("Horarios") }
            }
        }
    ) {
        if (!isActive) {
            AppCard {
                Text("Este negocio aún no acepta citas", style = MaterialTheme.typography.titleMedium)
                Text("Está en revisión. Intenta más tarde.", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(Ui.Gap))
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Volver") }
            }
            return@ScreenScaffold
        }

        if (state.loading && state.services.isEmpty()) {
            CenterLoading()
            return@ScreenScaffold
        }

        AppCard {
            Text("Datos", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = clientName,
                onValueChange = { clientName = it },
                label = { Text("Tu nombre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !state.loading
            )

            Spacer(Modifier.height(8.dp))

            var expanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { if (!state.loading) expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = state.selectedService?.let { "${it.name} (${it.durationMinutes} min)" } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Servicio") },
                    placeholder = { Text("Selecciona un servicio") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    enabled = !state.loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    state.services.forEach { s ->
                        DropdownMenuItem(
                            text = { Text("${s.name} (${s.durationMinutes} min)") },
                            onClick = {
                                vm.selectService(s)
                                expanded = false
                            }
                        )
                    }
                }
            }

            state.dayHours?.let { h ->
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (h.closed) "Horario: CERRADO" else "Horario: ${h.openTime} - ${h.closeTime}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            state.selectedService?.let { s ->
                Spacer(Modifier.height(6.dp))
                Text(
                    text = buildString {
                        append("Duración: ${s.durationMinutes} min")
                        if (!endPreview.isNullOrBlank() && state.time.isNotBlank()) {
                            append(" · Termina aprox: $endPreview")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { pickDate() },
                    enabled = !state.loading,
                    modifier = Modifier.weight(1f)
                ) { Text(if (state.date.isBlank()) "Fecha" else state.date) }

                OutlinedButton(
                    onClick = { pickTime() },
                    enabled = !state.loading && state.date.isNotBlank() && state.selectedService != null,
                    modifier = Modifier.weight(1f)
                ) { Text(if (state.time.isBlank()) "Hora" else state.time) }
            }

            if (isPast && state.date.isNotBlank() && state.time.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Esa hora ya pasó. Elige otra.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (state.loading) {
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            state.error?.let { err ->
                Spacer(Modifier.height(10.dp))
                Text(
                    text = err,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(Ui.Gap))

            Button(
                onClick = {
                    vm.create(
                        tenantId = tenantId,
                        clientName = clientName,
                        onSuccess = { navController.popBackStack() }
                    )
                },
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar cita")
            }
        }
    }
}
