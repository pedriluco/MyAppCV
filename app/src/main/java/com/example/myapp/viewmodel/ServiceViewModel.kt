package com.example.myapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.network.ApiClient
import com.example.myapp.network.CreateServiceRequest
import com.example.myapp.network.ServiceDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ServicesUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val items: List<ServiceDto> = emptyList(),
    val creating: Boolean = false
)

class ServiceViewModel : ViewModel() {

    private val _state = MutableStateFlow(ServicesUiState())
    val state = _state.asStateFlow()

    fun load(tenantId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching {
                ApiClient.serviceApi.list(tenantId)
            }.onSuccess {
                _state.value = ServicesUiState(items = it)
            }.onFailure { e ->
                _state.value = ServicesUiState(error = e.message ?: "Error")
            }
        }
    }

    fun create(tenantId: Long, name: String, duration: Int) {
        if (name.isBlank() || duration <= 0) return

        viewModelScope.launch {
            _state.value = _state.value.copy(creating = true)
            runCatching {
                ApiClient.serviceApi.create(
                    tenantId,
                    CreateServiceRequest(name.trim(), duration)
                )
            }.onSuccess {
                load(tenantId)
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    creating = false,
                    error = e.message ?: "Error"
                )
            }
        }
    }

    fun toggleActive(tenantId: Long, service: ServiceDto) {
        viewModelScope.launch {
            runCatching {
                ApiClient.serviceApi.setActive(
                    tenantId,
                    service.id,
                    !service.active
                )
            }.onSuccess {
                load(tenantId)
            }
        }
    }
}
