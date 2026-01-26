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
import com.myapp.backend.tenant.entity.Tenant;
import com.myapp.backend.tenant.entity.TenantStatus;
import com.myapp.backend.tenant.repository.TenantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class AppointmentService {

    private final AppointmentRepository appointments;
    private final BusinessHoursRepository hours;
    private final BusinessServiceRepository services;
    private final TenantRepository tenants;
    private final AuthzService authz;

    public AppointmentService(
            AppointmentRepository appointments,
            BusinessHoursRepository hours,
            BusinessServiceRepository services,
            TenantRepository tenants,
            AuthzService authz
    ) {
        this.appointments = appointments;
        this.hours = hours;
        this.services = services;
        this.tenants = tenants;
        this.authz = authz;
    }

    public Appointment create(Long tenantId, CreateAppointmentRequest req, Long userId) {

        Tenant tenant = tenants.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Business not found"));

        if (tenant.getStatus() != TenantStatus.ACTIVE) {
            authz.requireTenantAccess(tenantId, userId);
        }

        if (req.getDate() == null || req.getTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date and time are required");
        }

        if (req.getServiceId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request body");
        }

        String dateStr = req.getDate();
        String timeStr = req.getTime();

        if (dateStr.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date required (yyyy-MM-dd)");
        if (timeStr.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "time required (HH:mm)");
        if (req.getClientName() == null || req.getClientName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clientName required");
        }

        LocalDate date = LocalDate.parse(dateStr.trim());
        int dayOfWeek = date.getDayOfWeek().getValue();

        BusinessHours h = hours
                .findByTenantIdAndDayOfWeek(tenantId, dayOfWeek)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Business hours not found"));

        if (Boolean.TRUE.equals(h.getClosed())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Business is closed");
        }

        LocalTime startTime = LocalTime.parse(timeStr.trim());
        LocalTime open = LocalTime.parse(h.getOpenTime());
        LocalTime close = LocalTime.parse(h.getCloseTime());

        if (startTime.isBefore(open) || !startTime.isBefore(close)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Outside business hours");
        }

        BusinessService service = services.findById(req.getServiceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));

        int durationMinutes = service.getDurationMinutes();

        LocalDateTime startAt = LocalDateTime.of(date, startTime);
        LocalDateTime endAt = startAt.plusMinutes(durationMinutes);

        if (endAt.toLocalTime().isAfter(close)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Appointment exceeds business hours");
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        if (!a.getTenantId().equals(tenantId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Appointment does not belong to tenant");
        }

        a.setStatus(AppointmentStatus.APPROVED);
        return appointments.save(a);
    }

    public Appointment reject(Long tenantId, Long id, Long userId) {

        authz.requireTenantAccess(tenantId, userId);

        Appointment a = appointments.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        if (!a.getTenantId().equals(tenantId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Appointment does not belong to tenant");
        }

        a.setStatus(AppointmentStatus.REJECTED);
        return appointments.save(a);
    }
}
