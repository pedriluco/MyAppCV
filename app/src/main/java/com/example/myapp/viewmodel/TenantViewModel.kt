package com.example.myapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.model.Tenant
import com.example.myapp.data.repository.TenantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TenantUiState(
    val isLoading: Boolean = false,
    val tenants: List<Tenant> = emptyList(),
    val error: String? = null,
    val justCreated: Boolean = false
)

class TenantViewModel : ViewModel() {

    private val repository = TenantRepository()

    private val _uiState = MutableStateFlow(TenantUiState())
    val uiState: StateFlow<TenantUiState> = _uiState

    fun resetJustCreated() {
        _uiState.value = _uiState.value.copy(justCreated = false)
    }

    fun loadAllTenants() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val list = repository.getAllTenants()
                _uiState.value = _uiState.value.copy(isLoading = false, tenants = list)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error cargando negocios"
                )
            }
        }
    }

    fun createTenant(name: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, justCreated = false)
            try {
                repository.createTenant(name)
                val list = repository.getAllTenants()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    tenants = list,
                    justCreated = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error creando negocio",
                    justCreated = false
                )
            }
        }
    }

    fun approveTenant(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                repository.approveTenant(id)
                val list = repository.getAllTenants()
                _uiState.value = _uiState.value.copy(isLoading = false, tenants = list)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error aprobando negocio"
                )
            }
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val list = if (query.isBlank()) {
                    repository.getAllTenants()
                } else {
                    repository.searchTenants(query)
                }
                _uiState.value = _uiState.value.copy(isLoading = false, tenants = list)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error buscando negocios"
                )
            }
        }
    }
}
