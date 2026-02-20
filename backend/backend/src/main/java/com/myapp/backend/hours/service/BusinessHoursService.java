package com.myapp.backend.hours.service;

import com.myapp.backend.auth.service.AuthzService;
import com.myapp.backend.hours.dto.BusinessHoursRequest;
import com.myapp.backend.hours.entity.BusinessHours;
import com.myapp.backend.hours.repository.BusinessHoursRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BusinessHoursService {

    private final BusinessHoursRepository repository;
    private final AuthzService authz;

    public BusinessHoursService(BusinessHoursRepository repository, AuthzService authz) {
        this.repository = repository;
        this.authz = authz;
    }

    public List<BusinessHours> list(Long tenantId) {
        return repository.findByTenantId(tenantId);
    }

    public void upsert(Long tenantId, BusinessHoursRequest req, Long userId) {
        authz.requireTenantAccess(tenantId, userId);

        if (req == null || req.items == null) return;

        for (BusinessHoursRequest.Item it : req.items) {
            if (it == null || it.dayOfWeek == null) continue;

            BusinessHours hours = repository
                    .findByTenantIdAndDayOfWeek(tenantId, it.dayOfWeek)
                    .orElseGet(() -> {
                        BusinessHours h = new BusinessHours();
                        h.setTenantId(tenantId);
                        h.setDayOfWeek(it.dayOfWeek);
                        return h;
                    });

            hours.setClosed(it.closed != null && it.closed);

            if (hours.getClosed()) {
                hours.setOpenTime(null);
                hours.setCloseTime(null);
            } else {
                hours.setOpenTime(it.openTime);
                hours.setCloseTime(it.closeTime);
            }

            repository.save(hours);
        }
    }

    public BusinessHours getForDateOrThrow(Long tenantId, LocalDate date) {
        int day = date.getDayOfWeek().getValue();
        return repository.findByTenantIdAndDayOfWeek(tenantId, day)
                .orElseThrow();
    }
}