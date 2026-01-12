package com.myapp.backend.appointments.dto;

public class CreateAppointmentRequest {
    private Long tenantId;
    private Long serviceId;
    private String clientName;
    private String date; // "YYYY-MM-DD"
    private String time; // "HH:mm"

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
}
