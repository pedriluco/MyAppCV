package com.myapp.backend.appointments.service;

import com.myapp.backend.appointments.dto.CreateAppointmentRequest;
import com.myapp.backend.appointments.entity.Appointment;
import com.myapp.backend.appointments.entity.AppointmentStatus;
import com.myapp.backend.appointments.repository.AppointmentRepository;
import com.myapp.backend.auth.service.AuthzService;
import com.myapp.backend.hours.entity.BusinessHours;
import com.myapp.backend.hours.service.BusinessHoursService;
import com.myapp.backend.services.repository.BusinessServiceRepository;
import com.myapp.backend.tenant.repository.TenantRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepo;
    private final BusinessServiceRepository businessServiceRepo;
    private final BusinessHoursService businessHoursService;
    private final AuthzService authz;
    private final TenantRepository tenantRepo;

    public AppointmentService(
            AppointmentRepository appointmentRepo,
            BusinessServiceRepository businessServiceRepo,
            BusinessHoursService businessHoursService,
            AuthzService authz,
            TenantRepository tenantRepo
    ) {
        this.appointmentRepo = appointmentRepo;
        this.businessServiceRepo = businessServiceRepo;
        this.businessHoursService = businessHoursService;
        this.authz = authz;
        this.tenantRepo = tenantRepo;
    }

    public Appointment create(Long tenantId, CreateAppointmentRequest req) {
        authz.requireTenantAccess(tenantId);

        var service = businessServiceRepo
                .findByIdAndTenantId(req.getServiceId(), tenantId)
                .orElseThrow(() -> new RuntimeException("Service not found for this business"));

        int duration = service.getDurationMinutes();
        if (duration <= 0) throw new RuntimeException("Service durationMinutes must be > 0");

        LocalDate date = LocalDate.parse(req.getDate());
        LocalTime time = LocalTime.parse(req.getTime());
        LocalDateTime startAt = LocalDateTime.of(date, time);
        LocalDateTime endAt = startAt.plusMinutes(duration);

        BusinessHours h = businessHoursService.getForDateOrThrow(tenantId, date);
        if (Boolean.TRUE.equals(h.getClosed())) throw new RuntimeException("Business closed that day");

        LocalTime open = LocalTime.parse(h.getOpenTime());
        LocalTime close = LocalTime.parse(h.getCloseTime());

        if (startAt.toLocalTime().isBefore(open)) {
            throw new RuntimeException("Outside business hours (before open)");
        }
        if (endAt.toLocalTime().isAfter(close)) {
            throw new RuntimeException("Outside business hours (after close)");
        }

        boolean overlap = appointmentRepo.existsByTenantIdAndStatusAndStartAtLessThanAndEndAtGreaterThan(
                tenantId,
                AppointmentStatus.APPROVED,
                endAt,
                startAt
        );
        if (overlap) throw new RuntimeException("Overlaps with an approved appointment");

        Appointment a = new Appointment();
        a.setTenantId(tenantId);
        a.setServiceId(req.getServiceId());
        a.setClientName(req.getClientName());
        a.setStartAt(startAt);
        a.setEndAt(endAt);
        a.setStatus(AppointmentStatus.REQUESTED);

        return appointmentRepo.save(a);
    }

    public Appointment approve(Long tenantId, Long appointmentId, Long userId) {
        authz.assertOwnerOrAdmin(userId, tenantId);

        Appointment a = appointmentRepo.findByIdAndTenantId(appointmentId, tenantId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (a.getStatus() != AppointmentStatus.REQUESTED) {
            throw new RuntimeException("Only REQUESTED appointments can be approved");
        }

        boolean overlap = appointmentRepo.existsByTenantIdAndStatusAndStartAtLessThanAndEndAtGreaterThan(
                tenantId,
                AppointmentStatus.APPROVED,
                a.getEndAt(),
                a.getStartAt()
        );
        if (overlap) throw new RuntimeException("Overlaps with an approved appointment");

        a.setStatus(AppointmentStatus.APPROVED);
        return appointmentRepo.save(a);
    }

    public Appointment reject(Long tenantId, Long appointmentId, Long userId) {
        authz.assertOwnerOrAdmin(userId, tenantId);

        Appointment a = appointmentRepo.findByIdAndTenantId(appointmentId, tenantId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (a.getStatus() != AppointmentStatus.REQUESTED) {
            throw new RuntimeException("Only REQUESTED appointments can be rejected");
        }

        a.setStatus(AppointmentStatus.REJECTED);
        return appointmentRepo.save(a);
    }
}
