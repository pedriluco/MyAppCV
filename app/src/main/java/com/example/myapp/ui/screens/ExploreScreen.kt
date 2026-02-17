package com.example.myapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapp.data.model.Tenant
import com.example.myapp.ui.AppCard
import com.example.myapp.ui.CenterLoading
import com.example.myapp.ui.CenterText
import com.example.myapp.ui.ScreenScaffold
import com.example.myapp.ui.Ui
import com.example.myapp.viewmodel.TenantViewModel

@Composable
fun ExploreScreen(
    navController: NavController,
    tenantVm: TenantViewModel,
    isOwner: Boolean,
    isAdmin: Boolean,
    onGoToCreateAppointment: (tenantId: Long, status: String) -> Unit,
    onGoToAgenda: (Long) -> Unit,
    onGoToServices: (Long) -> Unit,
    onGoToHours: (Long) -> Unit
) {
    val state by tenantVm.uiState.collectAsState()

    var q by remember { mutableStateOf("") }
    var showOnlyActive by remember { mutableStateOf(!isOwner) } // USER: solo ACTIVE por default

    LaunchedEffect(Unit) {
        tenantVm.loadAllTenants()
    }

    val filtered = remember(state.tenants, q, showOnlyActive, isOwner) {
        val query = q.trim().lowercase()

        state.tenants
            .asSequence()
            .filter { t ->
                val nameOk = t.name?.lowercase()?.contains(query) ?: false
                if (query.isBlank()) true else nameOk
            }
            .filter { t ->
                val st = t.status ?: "PENDING"
                if (showOnlyActive) st == "ACTIVE" else true
            }
            // ACTIVE arriba siempre
            .sortedBy { (it.status ?: "PENDING") != "ACTIVE" }
            .toList()
    }

    ScreenScaffold(
        title = "Explorar",
        canBack = true,
        onBack = { navController.popBackStack() },
        scroll = false,
        actions = {
            if (!state.isLoading) {
                TextButton(onClick = { tenantVm.loadAllTenants() }) { Text("Recargar") }
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Ui.Gap)
        ) {
            OutlinedTextField(
                value = q,
                onValueChange = { q = it },
                label = { Text("Buscar negocio") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Solo ACTIVE", style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = showOnlyActive,
                    onCheckedChange = { showOnlyActive = it },
                    enabled = true // (si quieres: enabled = !isOwner)
                )
            }

            when {
                state.isLoading -> CenterLoading()
                state.error != null -> CenterText(state.error ?: "Error")
                filtered.isEmpty() -> CenterText("No hay resultados")
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(Ui.Gap)
                    ) {
                        items(filtered) { t ->
                            ExploreTenantCard(
                                tenant = t,
                                isOwner = isOwner,
                                isAdmin = isAdmin,
                                onGoToCreateAppointment = onGoToCreateAppointment,
                                onGoToAgenda = onGoToAgenda,
                                onGoToServices = onGoToServices,
                                onGoToHours = onGoToHours
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExploreTenantCard(
    tenant: Tenant,
    isOwner: Boolean,
    isAdmin: Boolean,
    onGoToCreateAppointment: (tenantId: Long, status: String) -> Unit,
    onGoToAgenda: (Long) -> Unit,
    onGoToServices: (Long) -> Unit,
    onGoToHours: (Long) -> Unit
) {
    val id = tenant.id?.toString()?.toLongOrNull() ?: 0L
    val status = tenant.status ?: "PENDING"

    val statusColor = when (status) {
        "ACTIVE" -> MaterialTheme.colorScheme.primary
        "PENDING" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outline
    }

    AppCard {
        Text(tenant.name ?: "Negocio", style = MaterialTheme.typography.titleMedium)

        Surface(
            color = statusColor.copy(alpha = 0.12f),
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = status,
                color = statusColor,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        Spacer(Modifier.height(10.dp))

        if (isOwner || isAdmin) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onGoToAgenda(id) },
                    modifier = Modifier.weight(1f)
                ) { Text("Agenda") }

                OutlinedButton(
                    onClick = { onGoToServices(id) },
                    modifier = Modifier.weight(1f)
                ) { Text("Servicios") }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = { onGoToHours(id) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Horarios") }
        } else {
            Button(
                onClick = { onGoToCreateAppointment(id, status) },
                enabled = status == "ACTIVE",
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (status == "ACTIVE") "Agendar cita" else "No disponible")
            }

            if (status != "ACTIVE") {
                Spacer(Modifier.height(6.dp))
                Text(
                    "Este negocio está en revisión. Aún no acepta citas.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

