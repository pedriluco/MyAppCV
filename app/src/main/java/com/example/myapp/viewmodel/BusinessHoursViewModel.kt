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

    fun load(tenantId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching {
                ApiClient.hoursApi.getAll(tenantId)
            }.onSuccess {
                _state.value = HoursUiState(items = it)
            }.onFailure { e ->
                _state.value = HoursUiState(error = e.message ?: "Error")
            }
        }
    }

    fun update(items: List<BusinessHoursDto>) {
        _state.value = _state.value.copy(items = items)
    }

    fun save(tenantId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(saving = true, error = null)
            runCatching {
                ApiClient.hoursApi.saveAll(tenantId, _state.value.items)
            }.onSuccess {
                _state.value = _state.value.copy(saving = false)
            }.onFailure { e ->
                _state.value = _state.value.copy(saving = false, error = e.message ?: "Error")
            }
        }
    }
}
