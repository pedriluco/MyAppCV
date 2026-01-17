package com.myapp.backend.hours.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "business_hours")
public class BusinessHours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @Column(nullable = false)
    private Integer dayOfWeek; // 1 = Lunes ... 7 = Domingo

    @Column
    private String openTime;   // "09:00"

    @Column
    private String closeTime;  // "18:00"

    @Column(nullable = false)
    private boolean closed;

    // ===== GETTERS =====
    public Long getId() {
        return id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public String getOpenTime() {
        return openTime;
    }

    public String getCloseTime() {
        return closeTime;
    }

    public boolean getClosed() {
        return closed;
    }

    // ===== SETTERS =====
    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public void setCloseTime(String closeTime) {
        this.closeTime = closeTime;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }
}
