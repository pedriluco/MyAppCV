package com.myapp.backend.appointments.controller;

import com.myapp.backend.appointments.dto.AppointmentResponse;
import com.myapp.backend.appointments.dto.CreateAppointmentRequest;
import com.myapp.backend.appointments.entity.Appointment;
import com.myapp.backend.appointments.repository.AppointmentRepository;
import com.myapp.backend.appointments.service.AppointmentService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/businesses/{tenantId}/appointments")
public class AppointmentController {

    private final AppointmentRepository repository;
    private final AppointmentService service;

    public AppointmentController(
            AppointmentRepository repository,
            AppointmentService service
    ) {
        this.repository = repository;
        this.service = service;
    }

    @GetMapping
    public List<AppointmentResponse> list(@PathVariable Long tenantId) {
        return repository.findByTenantId(tenantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping
    public AppointmentResponse create(
            @PathVariable Long tenantId,
            @RequestBody CreateAppointmentRequest req,
            Authentication authentication
    ) {
        Long userId = (authentication != null && authentication.getPrincipal() != null)
                ? (Long) authentication.getPrincipal()
                : null;

        Appointment saved = service.create(tenantId, req, userId);
        return toResponse(saved);
    }

    @PostMapping("/{id}/approve")
    public AppointmentResponse approve(
            @PathVariable Long tenantId,
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        Appointment saved = service.approve(tenantId, id, userId);
        return toResponse(saved);
    }

    @PostMapping("/{id}/reject")
    public AppointmentResponse reject(
            @PathVariable Long tenantId,
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        Appointment saved = service.reject(tenantId, id, userId);
        return toResponse(saved);
    }

    private AppointmentResponse toResponse(Appointment a) {
        return new AppointmentResponse(
                a.getId(),
                a.getTenantId(),
                a.getServiceId(),
                a.getClientName(),
                a.getStartAt().toString(),
                a.getEndAt().toString(),
                a.getStatus().name()
        );
    }
}
