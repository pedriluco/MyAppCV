package com.example.myapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.network.ApiClient
import com.example.myapp.network.AppointmentDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AgendaUiState(
    val loading: Boolean = false,
    val savingId: Long? = null,
    val error: String? = null,
    val items: List<AppointmentDto> = emptyList()
)

class AgendaViewModel : ViewModel() {

    private val _state = MutableStateFlow(AgendaUiState())
    val state = _state.asStateFlow()

    fun load(tenantId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching {
                ApiClient.appointmentApi.list(tenantId)
            }.onSuccess { list ->
                _state.value = _state.value.copy(
                    loading = false,
                    error = null,
                    items = list
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Error"
                )
            }
        }
    }

    fun approve(tenantId: Long, id: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(savingId = id, error = null)
            runCatching {
                ApiClient.appointmentApi.approve(tenantId, id)
            }.onSuccess {
                _state.value = _state.value.copy(savingId = null)
                load(tenantId)
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    savingId = null,
                    error = e.message ?: "Error"
                )
            }
        }
    }

    fun reject(tenantId: Long, id: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(savingId = id, error = null)
            runCatching {
                ApiClient.appointmentApi.reject(tenantId, id)
            }.onSuccess {
                _state.value = _state.value.copy(savingId = null)
                load(tenantId)
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    savingId = null,
                    error = e.message ?: "Error"
                )
            }
        }
    }
}
