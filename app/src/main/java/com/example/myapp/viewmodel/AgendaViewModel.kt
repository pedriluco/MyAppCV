package com.example.myapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.network.ApiClient
import com.example.myapp.network.AppointmentDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

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
                val sorted = list.sortedBy { it.startAt }
                _state.value = _state.value.copy(
                    loading = false,
                    error = null,
                    items = sorted
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    loading = false,
                    error = throwableMessage(e)
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
                    error = throwableMessage(e)
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
                    error = throwableMessage(e)
                )
            }
        }
    }

    private fun throwableMessage(t: Throwable): String {
        return when (t) {
            is HttpException -> {
                val code = t.code()
                val body = runCatching { t.response()?.errorBody()?.string() }.getOrNull().orEmpty()

                // intenta sacar "message":"..."
                val msg = Regex(""""message"\s*:\s*"([^"]+)"""")
                    .find(body)
                    ?.groupValues
                    ?.getOrNull(1)

                when {
                    !msg.isNullOrBlank() -> "HTTP $code: $msg"
                    body.isNotBlank() -> "HTTP $code"
                    else -> "HTTP $code"
                }
            }
            else -> t.message ?: "Error"
        }
    }
}
