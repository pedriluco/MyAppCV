package com.myapp.backend.services.dto;

public record CreateServiceRequest(
        String name,
        Integer durationMinutes
) {}
