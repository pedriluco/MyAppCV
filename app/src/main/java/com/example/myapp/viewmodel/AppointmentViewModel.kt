package com.example.myapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.model.AppointmentResponse
import com.example.myapp.data.model.CreateAppointmentRequest
import com.example.myapp.data.repository.AppointmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AppointmentUiState(
    val isLoading: Boolean = false,
    val appointments: List<AppointmentResponse> = emptyList(),
    val error: String? = null
)

class AppointmentViewModel(
    private val repo: AppointmentRepository = AppointmentRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppointmentUiState())
    val uiState: StateFlow<AppointmentUiState> = _uiState.asStateFlow()

    fun loadAppointments(tenantId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val list = repo.getAppointments(tenantId)
                _uiState.value = _uiState.value.copy(isLoading = false, appointments = list)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun createAppointment(
        tenantId: Long,
        serviceId: Long,
        clientName: String,
        date: String,
        time: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                repo.createAppointment(
                    CreateAppointmentRequest(
                        tenantId = tenantId,
                        serviceId = serviceId,
                        clientName = clientName,
                        date = date,
                        time = time
                    )
                )

                // refresca lista para que Agenda lo vea luego
                val list = repo.getAppointments(tenantId)
                _uiState.value = _uiState.value.copy(isLoading = false, appointments = list)

                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
