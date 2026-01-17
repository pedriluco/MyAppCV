package com.myapp.backend.appointments.dto;

public class CreateAppointmentRequest {
    private Long tenantId;
    private Long serviceId;
    private String clientName;
    private String date;
    private String time;

    public Long getTenantId() { return tenantId; }
    public Long getServiceId() { return serviceId; }
    public String getClientName() { return clientName; }
    public String getDate() { return date; }
    public String getTime() { return time; }

    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public void setDate(String date) { this.date = date; }
    public void setTime(String time) { this.time = time; }
}
