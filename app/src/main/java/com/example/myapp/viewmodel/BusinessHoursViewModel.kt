package com.example.myapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.network.ApiClient
import com.example.myapp.network.BusinessHoursDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HoursUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val items: List<BusinessHoursDto> = emptyList(),
    val saving: Boolean = false
)

class BusinessHoursViewModel : ViewModel() {

    private val _state = MutableStateFlow(HoursUiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: (HoursUiState) -> HoursUiState) {
        _state.value = reducer(_state.value)
    }

    fun load(tenantId: Long) {
        viewModelScope.launch {
            setState { it.copy(loading = true, error = null) }

            runCatching { ApiClient.hoursApi.getAll(tenantId) }
                .onSuccess { items ->
                    _state.value = HoursUiState(items = items)
                }
                .onFailure { e ->
                    _state.value = HoursUiState(error = e.message ?: "Error")
                }
        }
    }

    fun update(items: List<BusinessHoursDto>) {
        setState { it.copy(items = items) }
    }

    fun saveAndReload(tenantId: Long) {
        viewModelScope.launch {
            setState { it.copy(saving = true, error = null) }

            runCatching {
                val cleaned = _state.value.items.map { h ->
                    if (h.closed) h.copy(openTime = null, closeTime = null) else h
                }

                ApiClient.hoursApi.saveAll(tenantId, cleaned)
                ApiClient.hoursApi.getAll(tenantId)
            }.onSuccess { fresh ->
                _state.value = HoursUiState(items = fresh)
            }.onFailure { e ->
                setState { it.copy(saving = false, error = e.message ?: "Error") }
            }
        }
    }
}
