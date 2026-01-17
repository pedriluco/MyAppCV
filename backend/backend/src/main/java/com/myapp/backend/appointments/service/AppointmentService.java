package com.myapp.backend.appointments.service;

import com.myapp.backend.appointments.dto.CreateAppointmentRequest;
import com.myapp.backend.appointments.entity.Appointment;
import com.myapp.backend.appointments.entity.AppointmentStatus;
import com.myapp.backend.appointments.repository.AppointmentRepository;
import com.myapp.backend.auth.service.AuthzService;
import com.myapp.backend.hours.entity.BusinessHours;
import com.myapp.backend.hours.repository.BusinessHoursRepository;
import com.myapp.backend.services.entity.BusinessService;
import com.myapp.backend.services.repository.BusinessServiceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class AppointmentService {

    private final AppointmentRepository appointments;
    private final BusinessHoursRepository hours;
    private final BusinessServiceRepository services;
    private final AuthzService authz;

    public AppointmentService(
            AppointmentRepository appointments,
            BusinessHoursRepository hours,
            BusinessServiceRepository services,
            AuthzService authz
    ) {
        this.appointments = appointments;
        this.hours = hours;
        this.services = services;
        this.authz = authz;
    }

    public Appointment create(Long tenantId, CreateAppointmentRequest req, Long userId) {

        if (req.getDate() == null || req.getTime() == null) {
            throw new RuntimeException("Date and time are required");
        }

        if (req.getServiceId() == null) {
            throw new RuntimeException("Invalid request body");
        }

        String dateStr = req.getDate();
        String timeStr = req.getTime();

        if (dateStr.isBlank()) throw new RuntimeException("date required (yyyy-MM-dd)");
        if (timeStr.isBlank()) throw new RuntimeException("time required (HH:mm)");
        if (req.getClientName() == null || req.getClientName().isBlank()) {
            throw new RuntimeException("clientName required");
        }

        LocalDate date = LocalDate.parse(dateStr.trim());
        int dayOfWeek = date.getDayOfWeek().getValue();

        BusinessHours h = hours
                .findByTenantIdAndDayOfWeek(tenantId, dayOfWeek)
                .orElseThrow(() -> new RuntimeException("Business hours not found"));

        if (Boolean.TRUE.equals(h.getClosed())) {
            throw new RuntimeException("Business is closed");
        }

        LocalTime startTime = LocalTime.parse(timeStr.trim());
        LocalTime open = LocalTime.parse(h.getOpenTime());
        LocalTime close = LocalTime.parse(h.getCloseTime());

        if (startTime.isBefore(open) || !startTime.isBefore(close)) {
            throw new RuntimeException("Outside business hours");
        }

        BusinessService service = services.findById(req.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));

        int durationMinutes = service.getDurationMinutes();

        LocalDateTime startAt = LocalDateTime.of(date, startTime);
        LocalDateTime endAt = startAt.plusMinutes(durationMinutes);

        if (endAt.toLocalTime().isAfter(close)) {
            throw new RuntimeException("Appointment exceeds business hours");
        }

        Appointment a = new Appointment();
        a.setTenantId(tenantId);
        a.setServiceId(service.getId());
        a.setClientName(req.getClientName().trim());
        a.setStartAt(startAt);
        a.setEndAt(endAt);
        a.setStatus(AppointmentStatus.REQUESTED);

        return appointments.save(a);
    }

    public Appointment approve(Long tenantId, Long id, Long userId) {

        authz.requireTenantAccess(tenantId, userId);

        Appointment a = appointments.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!a.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Appointment does not belong to tenant");
        }

        a.setStatus(AppointmentStatus.APPROVED);
        return appointments.save(a);
    }

    public Appointment reject(Long tenantId, Long id, Long userId) {

        authz.requireTenantAccess(tenantId, userId);

        Appointment a = appointments.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!a.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Appointment does not belong to tenant");
        }

        a.setStatus(AppointmentStatus.REJECTED);
        return appointments.save(a);
    }
}
