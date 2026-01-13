package com.myapp.backend.services.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "services")
public class BusinessService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false)
    private Integer durationMinutes; // CLAVE

    @Column(nullable = false)
    private Boolean active = true;

    public BusinessService() {}

    public Long getId() {
        return id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public String getName() {
        return name;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public Boolean getActive() {
        return active;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
