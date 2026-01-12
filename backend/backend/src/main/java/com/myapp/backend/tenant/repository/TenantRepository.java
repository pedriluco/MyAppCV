package com.myapp.backend.repository;

import com.myapp.backend.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TenantRepository extends JpaRepository<Tenant, Long> {

    Optional<Tenant> findByIdAndStatus(Long id, String status);

    List<Tenant> findByNameContainingIgnoreCase(String name);
}
