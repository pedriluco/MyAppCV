package com.example.myapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val role: String = "USER",
    val businessName: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class RegisterViewModel(
    private val repo: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state = _state.asStateFlow()

    fun setEmail(v: String) { _state.value = _state.value.copy(email = v, error = null) }
    fun setPassword(v: String) { _state.value = _state.value.copy(password = v, error = null) }

    fun setRole(v: String) {
        val trimmed = v.trim().uppercase()
        val next = _state.value.copy(role = trimmed, error = null)
        _state.value = if (trimmed == "USER") next.copy(businessName = "") else next
    }

    fun setBusinessName(v: String) {
        _state.value = _state.value.copy(businessName = v, error = null)
    }

    fun consumeSuccess() {
        _state.value = _state.value.copy(success = false)
    }

    fun register() {
        val s = _state.value

        val email = s.email.trim()
        val pass = s.password
        val role = s.role.trim().uppercase()
        val biz = s.businessName.trim()

        if (email.isBlank() || pass.isBlank()) {
            _state.value = s.copy(error = "Email y password requeridos")
            return
        }
        if (role != "USER" && role != "OWNER") {
            _state.value = s.copy(error = "Rol inválido")
            return
        }
        if (role == "OWNER" && biz.isBlank()) {
            _state.value = s.copy(error = "Escribe el nombre del negocio")
            return
        }

        _state.value = s.copy(loading = true, error = null)

        viewModelScope.launch {
            repo.register(
                email = email,
                password = pass,
                role = role
            )
                .onSuccess {
                    _state.value = _state.value.copy(loading = false, success = true)
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        loading = false,
                        error = e.message ?: "Error registrando"
                    )
                }
        }
    }
}
