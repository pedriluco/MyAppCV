package com.myapp.backend.appointments;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

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
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime time;

    public Appointment() {}

    public Appointment(Long tenantId, Long serviceId, String clientName, LocalDate date, LocalTime time) {
        this.tenantId = tenantId;
        this.serviceId = serviceId;
        this.clientName = clientName;
        this.date = date;
        this.time = time;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public Long getServiceId() { return serviceId; }
    public String getClientName() { return clientName; }
    public LocalDate getDate() { return date; }
    public LocalTime getTime() { return time; }

    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setTime(LocalTime time) { this.time = time; }
}
