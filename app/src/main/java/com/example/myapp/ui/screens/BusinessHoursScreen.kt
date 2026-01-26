package com.example.myapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapp.ui.AppCard
import com.example.myapp.ui.CenterLoading
import com.example.myapp.ui.CenterText
import com.example.myapp.ui.ScreenScaffold
import com.example.myapp.ui.Ui
import com.example.myapp.viewmodel.BusinessHoursViewModel
import androidx.compose.ui.text.input.KeyboardType
import com.example.myapp.network.BusinessHoursDto



@Composable
fun BusinessHoursScreen(
    navController: NavController,
    tenantId: Long,
    vm: BusinessHoursViewModel = viewModel()
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(tenantId) {
        vm.load(tenantId)
    }

    ScreenScaffold(
        title = "Horarios",
        canBack = true,
        onBack = { navController.popBackStack() },
        scroll = false,
        actions = {
            TextButton(onClick = { vm.load(tenantId) }, enabled = !state.saving) { Text("Recargar") }
            TextButton(onClick = { vm.saveAndReload(tenantId) }, enabled = !state.saving) { Text("Guardar") }
        }
    ) {
        when {
            state.loading -> CenterLoading()

            state.error != null -> {
                AppCard {
                    Text("Error", style = MaterialTheme.typography.titleMedium)
                    Text(state.error ?: "Error", style = MaterialTheme.typography.bodyMedium)

                    Spacer(Modifier.height(Ui.Gap))

                    Button(
                        onClick = { vm.load(tenantId) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.saving
                    ) { Text("Reintentar") }
                }
            }

            state.items.isEmpty() -> CenterText("No hay horarios configurados")

            else -> {
                if (state.saving) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(Ui.Gap)
                ) {
                    itemsIndexed(state.items) { index, item ->
                        HoursItemCard(
                            item = item,
                            onChange = { updated ->
                                val newList = state.items.toMutableList()
                                newList[index] = updated
                                vm.update(newList)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HoursItemCard(
    item: BusinessHoursDto,
    onChange: (BusinessHoursDto) -> Unit
) {
    AppCard {
        Text(dayName(item.dayOfWeek), style = MaterialTheme.typography.titleMedium)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Cerrado", style = MaterialTheme.typography.bodyMedium)
            Switch(
                checked = item.closed,
                onCheckedChange = { closed ->
                    if (closed) {
                        onChange(item.copy(closed = true, openTime = null, closeTime = null))
                    } else {
                        // al abrir, pon valores por defecto si venían null
                        onChange(
                            item.copy(
                                closed = false,
                                openTime = item.openTime ?: "10:00",
                                closeTime = item.closeTime ?: "19:00"
                            )
                        )
                    }
                }
            )
        }

        if (!item.closed) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = item.openTime ?: "",
                    onValueChange = { txt ->
                        onChange(item.copy(openTime = txt.ifBlank { null }))
                    },
                    label = { Text("Abre") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = item.closeTime ?: "",
                    onValueChange = { txt ->
                        onChange(item.copy(closeTime = txt.ifBlank { null }))
                    },
                    label = { Text("Cierra") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
            }

            if (item.openTime.isNullOrBlank() || item.closeTime.isNullOrBlank()) {
                Text("Falta hora de apertura o cierre", style = MaterialTheme.typography.bodySmall)
            } else {
                Text("Formato sugerido: 10:00 / 19:00", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun dayName(day: Int): String = when (day) {
    1 -> "Lunes"
    2 -> "Martes"
    3 -> "Miércoles"
    4 -> "Jueves"
    5 -> "Viernes"
    6 -> "Sábado"
    7 -> "Domingo"
    else -> "Día $day"
}
