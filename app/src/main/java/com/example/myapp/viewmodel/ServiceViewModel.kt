package com.example.myapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.network.ApiClient
import com.example.myapp.network.CreateServiceRequest
import com.example.myapp.network.ServiceDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

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
            }.onSuccess { list ->
                _state.value = _state.value.copy(
                    loading = false,
                    error = null,
                    items = list
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    loading = false,
                    error = throwableMessage(e)
                )
            }
        }
    }

    fun create(tenantId: Long, name: String, duration: Int) {
        if (name.isBlank() || duration <= 0) return

        viewModelScope.launch {
            _state.value = _state.value.copy(creating = true, error = null)
            runCatching {
                ApiClient.serviceApi.create(
                    tenantId,
                    CreateServiceRequest(name.trim(), duration)
                )
            }.onSuccess {
                // reload
                load(tenantId)
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    creating = false,
                    error = throwableMessage(e)
                )
            }
        }
    }

    fun toggleActive(tenantId: Long, service: ServiceDto) {
        viewModelScope.launch {
            _state.value = _state.value.copy(error = null)
            runCatching {
                ApiClient.serviceApi.setActive(
                    tenantId,
                    service.id,
                    !service.active
                )
            }.onSuccess {
                load(tenantId)
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    error = throwableMessage(e)
                )
            }
        }
    }

    private fun throwableMessage(t: Throwable): String {
        return when (t) {
            is HttpException -> {
                val code = t.code()
                val body = runCatching { t.response()?.errorBody()?.string() }.getOrNull()
                if (!body.isNullOrBlank()) "HTTP $code: $body" else "HTTP $code"
            }
            else -> t.message ?: "Error"
        }
    }
}
