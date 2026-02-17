package com.example.myapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.myapp.viewmodel.RegisterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    vm: RegisterViewModel,
    onBack: () -> Unit,
    onRegistered: (role: String, businessName: String) -> Unit
) {
    val state by vm.state.collectAsState()

    var wantsBusiness by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.role) {
        wantsBusiness = (state.role == "OWNER")
    }

    LaunchedEffect(state.success) {
        if (state.success) {
            onRegistered(state.role, state.businessName)
            vm.consumeSuccess()
        }
    }

    LaunchedEffect(wantsBusiness) {
        if (wantsBusiness) {
            if (state.role != "OWNER") vm.setRole("OWNER")
        } else {
            if (state.role != "USER") vm.setRole("USER")
            vm.setBusinessName("")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear cuenta") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Volver") }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            OutlinedTextField(
                value = state.email,
                onValueChange = vm::setEmail,
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.password,
                onValueChange = vm::setPassword,
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text("¿Quieres crear tu negocio?")
                    Text(
                        if (wantsBusiness) "Se creará tu cuenta como dueño (pendiente de aprobación)."
                        else "Se creará tu cuenta como usuario para agendar citas.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(
                    checked = wantsBusiness,
                    onCheckedChange = { if (!state.loading) wantsBusiness = it },
                    enabled = !state.loading
                )
            }

            if (wantsBusiness) {
                OutlinedTextField(
                    value = state.businessName,
                    onValueChange = vm::setBusinessName,
                    label = { Text("Nombre del negocio") },
                    singleLine = true,
                    enabled = !state.loading,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = { vm.register() },
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.loading) "Creando..." else "Crear cuenta")
            }

            Text(
                "Nota: El admin se gestiona aparte. Aquí solo creas usuarios o dueños.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
