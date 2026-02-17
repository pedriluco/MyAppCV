package com.example.myapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapp.data.model.Tenant
import com.example.myapp.ui.AppCard
import com.example.myapp.ui.CenterText
import com.example.myapp.ui.ScreenScaffold
import com.example.myapp.ui.Ui
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding


@Composable
fun HomeScreen(
    title: String = "Negocios",
    tenants: List<Tenant>,
    isOwner: Boolean,
    isAdmin: Boolean,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    onGoToCreateBusiness: () -> Unit,
    onGoToExplore: () -> Unit,
    onGoToCreateAppointment: (tenantId: Long, status: String) -> Unit,
    onGoToAgenda: (Long) -> Unit,
    onGoToServices: (Long) -> Unit,
    onGoToHours: (Long) -> Unit,
    onGoToAdminRequests: () -> Unit
) {
    ScreenScaffold(
        title = title,
        canBack = false,
        onBack = {},
        scroll = false,
        actions = {
            TextButton(onClick = onRefresh) { Text("Recargar") }
            TextButton(onClick = onLogout) { Text("Salir") }
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isOwner) {
                Button(
                    onClick = onGoToCreateBusiness,
                    modifier = Modifier.weight(1f)
                ) { Text("Crear negocio") }
            } else {
                OutlinedButton(
                    onClick = onGoToExplore,
                    modifier = Modifier.weight(1f)
                ) { Text("Explorar") }
            }

            if (isAdmin) {
                OutlinedButton(
                    onClick = onGoToAdminRequests,
                    modifier = Modifier.weight(1f)
                ) { Text("Admin") }
            }
        }

        Spacer(Modifier.height(Ui.Gap))

        if (tenants.isEmpty()) {
            CenterText("No hay negocios todavía")
            return@ScreenScaffold
        }

        val sortedTenants = tenants.sortedBy { it.status != "ACTIVE" }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Ui.Gap)
        ) {
            items(sortedTenants) { t ->
                TenantCard(
                    tenant = t,
                    isOwner = isOwner,
                    onGoToCreateAppointment = onGoToCreateAppointment,
                    onGoToAgenda = onGoToAgenda,
                    onGoToServices = onGoToServices,
                    onGoToHours = onGoToHours
                )
            }
        }
    }
}

@Composable
private fun TenantCard(
    tenant: Tenant,
    isOwner: Boolean,
    onGoToCreateAppointment: (Long, String) -> Unit,
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

        if (isOwner) {
            if (status == "PENDING") {
                Text(
                    "Tu negocio está en revisión",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
            }

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
