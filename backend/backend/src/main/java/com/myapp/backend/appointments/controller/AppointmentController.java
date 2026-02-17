package com.myapp.backend.appointments.controller;

import com.myapp.backend.appointments.dto.AppointmentResponse;
import com.myapp.backend.appointments.dto.CreateAppointmentRequest;
import com.myapp.backend.appointments.entity.Appointment;
import com.myapp.backend.appointments.service.AppointmentService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/businesses/{tenantId}/appointments")
public class AppointmentController {

    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    // ✅ NUEVO: availability (sin requireTenantAccess / sin userId)
    @GetMapping("/availability")
    public List<AppointmentResponse> availability(
            @PathVariable Long tenantId,
            @RequestParam String date
    ) {
        List<Appointment> items = service.availabilityByDate(tenantId, date.trim());
        return items.stream().map(this::toResponse).toList();
    }

    @GetMapping
    public List<AppointmentResponse> list(
            @PathVariable Long tenantId,
            @RequestParam(required = false) String date,
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);

        List<Appointment> items = (date == null || date.isBlank())
                ? service.listAll(tenantId, userId)
                : service.listByDate(tenantId, date.trim(), userId);

        return items.stream().map(this::toResponse).toList();
    }

    @PostMapping
    public AppointmentResponse create(
            @PathVariable Long tenantId,
            @RequestBody CreateAppointmentRequest req,
            Authentication authentication
    ) {
        System.out.println(">>> HIT CREATE APPOINTMENT <<<");
        Long userId = extractUserId(authentication);
        Appointment saved = service.create(tenantId, req, userId);
        return toResponse(saved);
    }

    @PostMapping("/{id}/approve")
    public AppointmentResponse approve(
            @PathVariable Long tenantId,
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);

        Appointment saved = service.approve(tenantId, id, userId);
        return toResponse(saved);
    }

    @PostMapping("/{id}/reject")
    public AppointmentResponse reject(
            @PathVariable Long tenantId,
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);

        Appointment saved = service.reject(tenantId, id, userId);
        return toResponse(saved);
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) return null;

        Object principal = authentication.getPrincipal();

        if (principal instanceof Long l) return l;

        if (principal instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException ignored) {
            }
        }

        return null;
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
