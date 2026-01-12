package com.myapp.backend.auth.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password; // hash

    @Enumerated(EnumType.STRING)
    private GlobalRole globalRole;

    // getters / setters
}

