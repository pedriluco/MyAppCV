package com.example.myapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.network.BusinessHoursDto
import com.example.myapp.viewmodel.BusinessHoursViewModel

private val dayNames = listOf(
    "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessHoursScreen(
    tenantId: Long,
    onBack: () -> Unit,
    vm: BusinessHoursViewModel = viewModel()
) {
    val state by vm.state.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    LaunchedEffect(tenantId) {
        vm.load(tenantId)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Horarios") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("<") }
                },
                actions = {
                    TextButton(
                        onClick = { vm.save(tenantId) },
                        enabled = !state.saving
                    ) {
                        Text(if (state.saving) "Guardando..." else "Guardar")
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

            LazyColumn {
                itemsIndexed(state.items) { index, item ->
                    DayRow(
                        name = dayNames[item.dayOfWeek - 1],
                        value = item,
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

@Composable
private fun DayRow(
    name: String,
    value: BusinessHoursDto,
    onChange: (BusinessHoursDto) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(name, style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = !value.closed,
                    onCheckedChange = {
                        onChange(value.copy(closed = !it))
                    }
                )
            }

            if (!value.closed) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = value.openTime ?: "",
                        onValueChange = { onChange(value.copy(openTime = it)) },
                        label = { Text("Abre") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = value.closeTime ?: "",
                        onValueChange = { onChange(value.copy(closeTime = it)) },
                        label = { Text("Cierra") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
