package com.myapp.backend.tenant.repository;

import com.myapp.backend.tenant.entity.Tenant;
import com.myapp.backend.tenant.entity.TenantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TenantRepository extends JpaRepository<Tenant, Long> {

    List<Tenant> findByStatus(TenantStatus status);

    List<Tenant> findByStatusAndNameContainingIgnoreCase(TenantStatus status, String name);

    @Query("""
        SELECT t FROM Tenant t
        JOIN Membership m ON m.businessId = t.id
        WHERE m.userId = :userId
    """)
    List<Tenant> findAllForUser(@Param("userId") Long userId);
}