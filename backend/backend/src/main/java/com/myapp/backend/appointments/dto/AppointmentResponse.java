package com.myapp.backend.appointments.dto;

public class AppointmentResponse {
    private Long id;
    private Long tenantId;
    private Long serviceId;
    private String clientName;
    private String startAt;
    private String endAt;
    private String status;

    public AppointmentResponse(Long id, Long tenantId, Long serviceId, String clientName, String startAt, String endAt, String status) {
        this.id = id;
        this.tenantId = tenantId;
        this.serviceId = serviceId;
        this.clientName = clientName;
        this.startAt = startAt;
        this.endAt = endAt;
        this.status = status;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public Long getServiceId() { return serviceId; }
    public String getClientName() { return clientName; }
    public String getStartAt() { return startAt; }
    public String getEndAt() { return endAt; }
    public String getStatus() { return status; }
}


