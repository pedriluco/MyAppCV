package com.myapp.backend.services.repository;

import com.myapp.backend.services.entity.BusinessService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusinessServiceRepository extends JpaRepository<BusinessService, Long> {

    List<BusinessService> findByTenantId(Long tenantId);

    List<BusinessService> findByTenantIdAndActiveTrue(Long tenantId);

    Optional<BusinessService> findByIdAndTenantId(Long id, Long tenantId);
}
