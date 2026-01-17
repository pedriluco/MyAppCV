package com.myapp.backend.services.dto;

public record ServiceResponse(
        Long id,
        Long tenantId,
        String name,
        Integer durationMinutes,
        Boolean active
) {}
