package com.myapp.backend.tenant.repository;

import com.myapp.backend.tenant.entity.Tenant;
import com.myapp.backend.tenant.entity.TenantStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TenantRepository extends JpaRepository<Tenant, Long> {

    List<Tenant> findByStatus(TenantStatus status);

    List<Tenant> findByStatusAndNameContainingIgnoreCase(TenantStatus status, String name);
}
