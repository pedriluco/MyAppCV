package com.myapp.backend.appointments.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @Column(nullable = false)
    private Long serviceId;

    @Column(nullable = false, length = 120)
    private String clientName;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status = AppointmentStatus.REQUESTED;

    public Appointment() {}

    // getters
    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public Long getServiceId() { return serviceId; }
    public String getClientName() { return clientName; }
    public LocalDateTime getStartAt() { return startAt; }
    public LocalDateTime getEndAt() { return endAt; }
    public AppointmentStatus getStatus() { return status; }

    // setters
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
    public void setStatus(AppointmentStatus status) { this.status = status; }
}
