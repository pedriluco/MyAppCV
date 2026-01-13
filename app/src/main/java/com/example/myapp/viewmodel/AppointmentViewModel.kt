package com.example.myapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.network.ApiClient
import com.example.myapp.network.CreateAppointmentRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AppointmentUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

class AppointmentViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AppointmentUiState())
    val uiState = _uiState.asStateFlow()

    fun createAppointment(
        tenantId: Long,
        serviceId: Long,
        clientName: String,
        date: String,
        time: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = AppointmentUiState(isLoading = true, error = null)

            val startAtIso = "${date.trim()}T${time.trim()}:00"

            runCatching {
                ApiClient.appointmentApi.create(
                    tenantId,
                    CreateAppointmentRequest(
                        serviceId = serviceId,
                        clientName = clientName.trim(),
                        startAt = startAtIso
                    )
                )
            }.onSuccess {
                _uiState.value = AppointmentUiState(isLoading = false, error = null)
                onSuccess()
            }.onFailure { e ->
                _uiState.value = AppointmentUiState(
                    isLoading = false,
                    error = e.message ?: "Error"
                )
            }
        }
    }
}
