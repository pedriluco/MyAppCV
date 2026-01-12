package com.myapp.backend.services.dto;

public record CreateServiceRequest(
        Long tenantId,
        String name,
        Integer durationMinutes
) {}
