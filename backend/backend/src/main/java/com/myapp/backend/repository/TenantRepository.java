package com.myapp.backend.repository;

import com.myapp.backend.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
    List<Tenant> findByNameContainingIgnoreCase(String name);
}
