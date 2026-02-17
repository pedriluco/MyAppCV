package com.example.myapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.network.ApiClient
import com.example.myapp.network.AppointmentDto
import com.example.myapp.network.BusinessHoursDto
import com.example.myapp.network.CreateAppointmentRequest
import com.example.myapp.network.ServiceDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.util.Calendar
import java.util.Locale

data class CreateAppointmentUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val services: List<ServiceDto> = emptyList(),
    val selectedService: ServiceDto? = null,
    val date: String = "",
    val time: String = "",
    val hoursByDay: Map<Int, BusinessHoursDto> = emptyMap(),
    val dayHours: BusinessHoursDto? = null,
    val dayAppointments: List<AppointmentDto> = emptyList(),
    val canSubmit: Boolean = false
)

class CreateAppointmentViewModel : ViewModel() {

    private val _state = MutableStateFlow(CreateAppointmentUiState())
    val state = _state.asStateFlow()

    fun loadServices(tenantId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null).recalc()
            runCatching {
                ApiClient.serviceApi.list(tenantId)
            }.onSuccess { list ->
                val active = list.filter { it.active }
                val s = _state.value
                _state.value = s.copy(
                    loading = false,
                    services = active,
                    selectedService = s.selectedService?.takeIf { sel -> active.any { it.id == sel.id } }
                ).recalc()
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    loading = false,
                    error = readableError(e, "Error cargando servicios")
                ).recalc()
            }
        }
    }

    fun loadHours(tenantId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(error = null).recalc()
            runCatching {
                ApiClient.hoursApi.getAll(tenantId)
            }.onSuccess { list ->
                val map = list.associateBy { it.dayOfWeek }
                val s = _state.value
                val day = s.date.takeIf { it.isNotBlank() }?.let { dayOfWeekFromIsoDate(it) }
                _state.value = s.copy(
                    hoursByDay = map,
                    dayHours = day?.let { map[it] }
                ).recalc()
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    error = readableError(e, "Error cargando horarios")
                ).recalc()
            }
        }
    }

    fun loadAppointmentsForDay(tenantId: Long, date: String) {
        viewModelScope.launch {
            runCatching {
                ApiClient.appointmentApi.availabilityByDate(
                    tenantId,
                    date.trim()
                )
            }.onSuccess { items ->
                _state.value = _state.value
                    .copy(dayAppointments = items)
                    .recalc()
            }.onFailure {
                _state.value = _state.value
                    .copy(error = "No se pudo cargar disponibilidad")
                    .recalc()
            }
        }
    }

    fun selectService(tenantId: Long, svc: ServiceDto) {
        _state.value = _state.value.copy(selectedService = svc, error = null).recalc()
        val date = _state.value.date
        if (date.isNotBlank()) loadAppointmentsForDay(tenantId, date)
        if (_state.value.time.isNotBlank() && !isTimeValidForBusiness()) {
            _state.value = _state.value.copy(
                time = "",
                error = "La hora elegida ya no cabe con la duración"
            ).recalc()
        }
    }

    fun setDate(tenantId: Long, v: String) {
        val day = dayOfWeekFromIsoDate(v)
        val hours = _state.value.hoursByDay[day]
        _state.value = _state.value.copy(date = v, dayHours = hours, error = null).recalc()

        loadAppointmentsForDay(tenantId, v)

        if (hours?.closed == true) {
            _state.value = _state.value.copy(time = "", error = "Ese día el negocio está cerrado").recalc()
            return
        }
        if (_state.value.time.isNotBlank() && !isTimeValidForBusiness()) {
            _state.value = _state.value.copy(time = "", error = "La hora no cae dentro del horario").recalc()
        }
    }

    fun setTime(v: String) {
        _state.value = _state.value.copy(time = v, error = null).recalc()
        if (!isTimeValidForBusiness()) {
            _state.value = _state.value.copy(
                time = "",
                error = "Fuera de horario o no cabe con la duración"
            ).recalc()
        }
    }

    fun create(
        tenantId: Long,
        clientName: String,
        onSuccess: () -> Unit
    ) {
        val s = _state.value
        val service = s.selectedService ?: run {
            _state.value = s.copy(error = "Selecciona un servicio").recalc()
            return
        }
        if (clientName.isBlank()) {
            _state.value = s.copy(error = "Escribe tu nombre").recalc()
            return
        }
        if (s.date.isBlank() || s.time.isBlank()) {
            _state.value = s.copy(error = "Selecciona fecha y hora").recalc()
            return
        }
        val h = s.dayHours ?: run {
            _state.value = s.copy(error = "No pude cargar el horario del día. Recarga.").recalc()
            return
        }
        if (h.closed) {
            _state.value = s.copy(error = "Ese día el negocio está cerrado").recalc()
            return
        }
        if (!isTimeValidForBusiness()) {
            _state.value = s.copy(error = "Hora inválida (fuera de horario o no cabe con la duración)").recalc()
            return
        }
        if (isSelectedDateTimeInPast()) {
            _state.value = s.copy(error = "Esa hora ya pasó").recalc()
            return
        }

        val req = CreateAppointmentRequest(
            tenantId = tenantId,
            serviceId = service.id,
            clientName = clientName.trim(),
            date = s.date.trim(),
            time = s.time.trim()
        )

        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null).recalc()

            runCatching {
                ApiClient.appointmentApi.create(tenantId, req)
            }.onSuccess {
                _state.value = _state.value.copy(loading = false, error = null).recalc()
                loadAppointmentsForDay(tenantId, s.date.trim())
                onSuccess()
            }.onFailure { e ->
                val msg = when (e) {
                    is HttpException -> {
                        val body = runCatching { e.response()?.errorBody()?.string() }
                            .getOrNull()
                            .orEmpty()

                        val backendMsg = Regex(""""message"\s*:\s*"([^"]+)"""")
                            .find(body)
                            ?.groupValues
                            ?.getOrNull(1)

                        when (e.code()) {
                            409 -> backendMsg ?: "Ese horario ya está ocupado"
                            400 -> backendMsg ?: "Datos inválidos"
                            403 -> "No tienes permiso para agendar"
                            else -> backendMsg ?: "Error ${e.code()}"
                        }
                    }
                    else -> e.message ?: "Error creando cita"
                }

                _state.value = _state.value.copy(
                    loading = false,
                    error = msg
                ).recalc()
            }
        }
    }

    fun allowedSlots(step: Int = 10): List<String> {
        val window = allowedTimeWindowMinutes() ?: return emptyList()
        val service = _state.value.selectedService ?: return emptyList()
        val dur = service.durationMinutes

        val taken = _state.value.dayAppointments.mapNotNull {
            runCatching {
                val s = it.startAt.substring(11, 16)
                val e = it.endAt.substring(11, 16)
                toMin(s) to toMin(e)
            }.getOrNull()
        }

        val out = mutableListOf<String>()
        var t = window.first
        while (t <= window.second) {
            val end = t + dur
            val clash = taken.any { (s, e) -> overlap(t, end, s, e) }
            if (!clash) out += toHHMM(t)
            t += step
        }
        return out
    }

    fun allowedTimeWindowMinutesPublic(): Pair<Int, Int>? = allowedTimeWindowMinutes()

    fun computeEndTimePreview(): String? {
        val s = _state.value
        val svc = s.selectedService ?: return null
        if (s.time.isBlank()) return null
        val startMin = hhmmToMinutes(s.time) ?: return null
        val endMin = startMin + svc.durationMinutes
        return toHHMM(endMin)
    }

    fun isSelectedDateTimeInPast(): Boolean {
        val s = _state.value
        if (s.date.isBlank() || s.time.isBlank()) return false
        return runCatching {
            val (y, mo, d) = parseIsoDate(s.date) ?: return@runCatching false
            val (hh, mm) = parseHHmm(s.time) ?: return@runCatching false
            val selected = Calendar.getInstance().apply {
                set(Calendar.YEAR, y)
                set(Calendar.MONTH, mo - 1)
                set(Calendar.DAY_OF_MONTH, d)
                set(Calendar.HOUR_OF_DAY, hh)
                set(Calendar.MINUTE, mm)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            selected.timeInMillis < System.currentTimeMillis()
        }.getOrDefault(false)
    }

    private fun overlap(aStart: Int, aEnd: Int, bStart: Int, bEnd: Int): Boolean {
        return aStart < bEnd && bStart < aEnd
    }

    private fun allowedTimeWindowMinutes(): Pair<Int, Int>? {
        val s = _state.value
        val h = s.dayHours ?: return null
        val svc = s.selectedService ?: return null
        if (h.closed || h.openTime == null || h.closeTime == null) return null
        val open = toMin(h.openTime)
        val close = toMin(h.closeTime)
        val dur = svc.durationMinutes
        val maxStart = close - dur
        if (maxStart < open) return null
        return open to maxStart
    }

    private fun isTimeValidForBusiness(): Boolean {
        val s = _state.value
        val h = s.dayHours ?: return true
        if (h.closed) return false
        if (s.time.isBlank()) return true
        val dur = s.selectedService?.durationMinutes ?: 0
        return runCatching {
            val startMin = hhmmToMinutes(s.time) ?: return@runCatching false
            val openMin = hhmmToMinutes(h.openTime ?: return@runCatching false) ?: return@runCatching false
            val closeMin = hhmmToMinutes(h.closeTime ?: return@runCatching false) ?: return@runCatching false
            if (startMin < openMin) return@runCatching false
            if (startMin >= closeMin) return@runCatching false
            val endMin = startMin + dur
            endMin <= closeMin
        }.getOrDefault(false)
    }

    private fun CreateAppointmentUiState.recalc(): CreateAppointmentUiState {
        val ok = selectedService != null && date.isNotBlank() && time.isNotBlank() && !loading
        return copy(canSubmit = ok)
    }

    private fun readableError(t: Throwable, fallback: String): String {
        return when (t) {
            is HttpException -> "HTTP ${t.code()}"
            else -> t.message ?: fallback
        }
    }

    private fun parseIsoDate(s: String): Triple<Int, Int, Int>? {
        val p = s.trim().split("-")
        if (p.size != 3) return null
        return runCatching {
            Triple(p[0].toInt(), p[1].toInt(), p[2].toInt())
        }.getOrNull()
    }

    private fun parseHHmm(s: String): Pair<Int, Int>? {
        val p = s.trim().split(":")
        if (p.size != 2) return null
        return runCatching {
            p[0].toInt() to p[1].toInt()
        }.getOrNull()
    }

    private fun hhmmToMinutes(s: String): Int? {
        val (hh, mm) = parseHHmm(s) ?: return null
        if (hh !in 0..23 || mm !in 0..59) return null
        return hh * 60 + mm
    }

    private fun toMin(hhmm: String): Int {
        val (h, m) = hhmm.split(":").map { it.toInt() }
        return h * 60 + m
    }

    private fun toHHMM(min: Int): String =
        String.format(Locale.US, "%02d:%02d", min / 60, min % 60)

    private fun dayOfWeekFromIsoDate(dateIso: String): Int {
        val (y, mo, d) = parseIsoDate(dateIso) ?: return 1
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, y)
            set(Calendar.MONTH, mo - 1)
            set(Calendar.DAY_OF_MONTH, d)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }
}
