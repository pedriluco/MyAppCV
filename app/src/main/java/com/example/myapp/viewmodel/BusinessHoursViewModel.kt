package com.example.myapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.network.ApiClient
import com.example.myapp.network.BusinessHoursDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class HoursUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val items: List<BusinessHoursDto> = emptyList(),
    val saving: Boolean = false
)

class BusinessHoursViewModel : ViewModel() {

    private val _state = MutableStateFlow(HoursUiState())
    val state = _state.asStateFlow()

    fun load(tenantId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)

            runCatching { ApiClient.hoursApi.getAll(tenantId) }
                .onSuccess { items ->
                    _state.value = _state.value.copy(
                        loading = false,
                        error = null,
                        items = items
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        loading = false,
                        error = throwableMessage(e)
                    )
                }
        }
    }

    fun update(items: List<BusinessHoursDto>) {
        _state.value = _state.value.copy(items = items)
    }

    fun saveAndReload(tenantId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(saving = true, error = null)

            runCatching {
                val cleaned = _state.value.items.map { h ->
                    if (h.closed) h.copy(openTime = null, closeTime = null) else h
                }
                ApiClient.hoursApi.saveAll(tenantId, cleaned)
            }.onSuccess { saved ->
                _state.value = _state.value.copy(
                    saving = false,
                    error = null,
                    items = saved
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    saving = false,
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
