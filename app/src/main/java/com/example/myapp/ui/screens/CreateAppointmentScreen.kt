package com.example.myapp.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapp.ui.AppCard
import com.example.myapp.ui.CenterLoading
import com.example.myapp.ui.ScreenScaffold
import com.example.myapp.ui.Routes
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
    role: String,
    vm: CreateAppointmentViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    val isActive = tenantStatus == "ACTIVE"
    val isOwnerOrAdmin = role == "OWNER" || role == "ADMIN"

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
                vm.setDate(
                    tenantId,
                    String.format(Locale.US, "%04d-%02d-%02d", yy, mm + 1, dd)
                )
            },
            y, m, d
        ).apply {
            datePicker.minDate = System.currentTimeMillis() - 1000
        }.show()
    }

    val slots = remember(
        state.date,
        state.selectedService?.id,
        state.dayHours,
        state.dayAppointments
    ) {
        vm.allowedSlots(step = 10)
    }

    var slotsExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.date, state.selectedService?.id) {
        slotsExpanded = false
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
            if (isActive && isOwnerOrAdmin) {
                TextButton(onClick = { navController.navigate(Routes.services(tenantId)) }) {
                    Text("Servicios")
                }
                TextButton(onClick = { navController.navigate(Routes.hours(tenantId)) }) {
                    Text("Horarios")
                }
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
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = !state.loading)
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    state.services.forEach { s ->
                        DropdownMenuItem(
                            text = { Text("${s.name} (${s.durationMinutes} min)") },
                            onClick = {
                                vm.selectService(tenantId, s)
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

            val canPickSlot =
                !state.loading &&
                        state.date.isNotBlank() &&
                        state.selectedService != null &&
                        slots.isNotEmpty()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { pickDate() },
                    enabled = !state.loading,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (state.date.isBlank()) "Fecha" else state.date)
                }

                ExposedDropdownMenuBox(
                    expanded = slotsExpanded,
                    onExpandedChange = { if (canPickSlot) slotsExpanded = !slotsExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = state.time,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Hora disponible") },
                        placeholder = { Text(if (slots.isEmpty()) "Sin horarios" else "Selecciona") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(slotsExpanded) },
                        enabled = canPickSlot,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = canPickSlot)
                    )

                    DropdownMenu(
                        expanded = slotsExpanded,
                        onDismissRequest = { slotsExpanded = false }
                    ) {
                        slots.forEach { hhmm ->
                            DropdownMenuItem(
                                text = { Text(hhmm) },
                                onClick = {
                                    vm.setTime(hhmm)
                                    slotsExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            if (
                state.date.isNotBlank() &&
                state.selectedService != null &&
                slots.isEmpty() &&
                state.dayHours?.closed == false
            ) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "No hay horarios disponibles (ocupado o no cabe la duración).",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
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
                        clientName = clientName.trim(),
                        onSuccess = {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("refreshAgenda", true)
                            navController.popBackStack()
                        }
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
