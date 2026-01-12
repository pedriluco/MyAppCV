package com.example.myapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.model.AppointmentResponse
import com.example.myapp.network.ApiClient
import kotlinx.coroutines.launch

class AgendaViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var appointments by mutableStateOf<List<AppointmentResponse>>(emptyList())
        private set

    fun loadAppointments(tenantId: Long) {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                appointments = ApiClient.appointmentApi.getAppointments(tenantId)
            } catch (e: Exception) {
                error = e.message ?: "Error cargando agenda"
            } finally {
                isLoading = false
            }
        }
    }
}
