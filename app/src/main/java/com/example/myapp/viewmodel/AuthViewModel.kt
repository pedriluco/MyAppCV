package com.example.myapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val role: String = "USER",
    val loading: Boolean = false,
    val error: String? = null,
    val loggedIn: Boolean = false,
    val checkedToken: Boolean = false
)

class AuthViewModel(
    private val repo: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state = _state.asStateFlow()

    fun setEmail(v: String) { _state.value = _state.value.copy(email = v) }
    fun setPassword(v: String) { _state.value = _state.value.copy(password = v) }

    fun isOwnerOrAdmin(): Boolean =
        state.value.role == "OWNER" || state.value.role == "ADMIN"

    fun isAdmin(): Boolean =
        state.value.role == "ADMIN"

    fun loadToken() {
        viewModelScope.launch {
            val hasToken = repo.loadTokenIntoClient()
            _state.value = _state.value.copy(
                loggedIn = hasToken,
                checkedToken = true
            )
        }
    }

    fun login() {
        val s = _state.value
        if (s.email.isBlank() || s.password.isBlank()) {
            _state.value = s.copy(error = "Email y password requeridos")
            return
        }

        _state.value = s.copy(loading = true, error = null)

        viewModelScope.launch {
            val result = repo.login(s.email.trim(), s.password)
            result.onSuccess {
                _state.value = _state.value.copy(
                    loading = false,
                    loggedIn = true,
                    checkedToken = true
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Error",
                    checkedToken = true
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repo.logout()
            _state.value = AuthUiState(checkedToken = true, loggedIn = false)
        }
    }
}
