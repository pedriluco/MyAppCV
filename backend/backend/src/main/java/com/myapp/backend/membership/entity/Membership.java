package com.myapp.backend.membership.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "memberships",
        uniqueConstraints = @UniqueConstraint(columnNames = {"business_id", "user_id"})
)
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    private MembershipRole role;

    @Enumerated(EnumType.STRING)
    private MembershipStatus status;

    // getters / setters
}
