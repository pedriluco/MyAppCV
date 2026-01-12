package com.example.myapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.viewmodel.AgendaViewModel
import com.example.myapp.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(
    tenantId: Long,
    authVm: AuthViewModel,
    onBack: () -> Unit,
    vm: AgendaViewModel = viewModel()
) {
    val state by vm.state.collectAsState()

    fun pretty(dt: String): String =
        dt.replace("T", " ").substring(0, 16)

    LaunchedEffect(tenantId) {
        vm.load(tenantId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agenda") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("<") }
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

            if (state.loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
            }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
            }

            LazyColumn {
                items(state.items) { a ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(a.clientName, style = MaterialTheme.typography.titleMedium)
                            Text("${pretty(a.startAt)} → ${pretty(a.endAt)}")
                            Text("Status: ${a.status}")

                            if (a.status == "REQUESTED" && authVm.isOwnerOrAdmin()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = { vm.reject(tenantId, a.id) },
                                        enabled = !state.loading
                                    ) {
                                        Text("Rechazar")
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Button(
                                        onClick = { vm.approve(tenantId, a.id) },
                                        enabled = !state.loading
                                    ) {
                                        Text("Aprobar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
