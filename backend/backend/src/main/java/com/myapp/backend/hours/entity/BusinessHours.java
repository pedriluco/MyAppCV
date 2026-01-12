package com.myapp.backend.hours.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "business_hours",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "day_of_week"})
)
public class BusinessHours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek; // 1=Lunes ... 7=Domingo

    @Column(nullable = false)
    private String openTime;   // "09:00"

    @Column(nullable = false)
    private String closeTime;  // "18:00"

    @Column(nullable = false)
    private Boolean closed = false;

    public BusinessHours() {}

    // getters / setters
}
