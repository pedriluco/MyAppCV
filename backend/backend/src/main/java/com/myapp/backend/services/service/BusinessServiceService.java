package com.myapp.backend.services.service;

import com.myapp.backend.auth.service.AuthzService;
import com.myapp.backend.services.dto.CreateServiceRequest;
import com.myapp.backend.services.entity.BusinessService;
import com.myapp.backend.services.repository.BusinessServiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BusinessServiceService {

    private final BusinessServiceRepository repository;
    private final AuthzService authz;

    public BusinessServiceService(BusinessServiceRepository repository, AuthzService authz) {
        this.repository = repository;
        this.authz = authz;
    }

    public List<BusinessService> listActive(Long tenantId) {
        return repository.findByTenantIdAndActiveTrue(tenantId);
    }

    public BusinessService create(Long tenantId, CreateServiceRequest req, Long userId) {
        authz.assertOwnerOrAdmin(userId, tenantId);

        if (req.name == null || req.name.isBlank()) throw new RuntimeException("name required");
        if (req.durationMinutes == null || req.durationMinutes <= 0) throw new RuntimeException("durationMinutes must be > 0");

        BusinessService s = new BusinessService();
        s.setTenantId(tenantId);
        s.setName(req.name.trim());
        s.setDurationMinutes(req.durationMinutes);
        s.setActive(true);

        return repository.save(s);
    }

    public void setActive(Long tenantId, Long serviceId, Boolean active, Long userId) {
        authz.assertOwnerOrAdmin(userId, tenantId);

        BusinessService s = repository.findByIdAndTenantId(serviceId, tenantId).orElseThrow();
        s.setActive(active != null && active);
        repository.save(s);
    }
}
