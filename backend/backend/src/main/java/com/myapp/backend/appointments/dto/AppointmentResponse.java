package com.myapp.backend.appointments.dto;

public class AppointmentResponse {
    public Long id;
    public Long tenantId;
    public Long serviceId;
    public String clientName;
    public String date; // "2026-01-15"
    public String time; // "16:00"

    public AppointmentResponse(Long id, Long tenantId, Long serviceId, String clientName, String date, String time) {
        this.id = id;
        this.tenantId = tenantId;
        this.serviceId = serviceId;
        this.clientName = clientName;
        this.date = date;
        this.time = time;
    }
}
