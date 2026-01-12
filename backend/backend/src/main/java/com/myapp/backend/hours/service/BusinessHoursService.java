package com.myapp.backend.hours.service;

import com.myapp.backend.auth.service.AuthzService;
import com.myapp.backend.hours.dto.BusinessHoursRequest;
import com.myapp.backend.hours.entity.BusinessHours;
import com.myapp.backend.hours.repository.BusinessHoursRepository;
import org.springframework.stereotype.Service;

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
        authz.assertOwnerOrAdmin(userId, tenantId);

        BusinessHours hours = repository
                .findByTenantIdAndDayOfWeek(tenantId, req.dayOfWeek)
                .orElseGet(() -> {
                    BusinessHours h = new BusinessHours();
                    h.setTenantId(tenantId);
                    h.setDayOfWeek(req.dayOfWeek);
                    return h;
                });

        hours.setOpenTime(req.openTime);
        hours.setCloseTime(req.closeTime);
        hours.setClosed(req.closed != null && req.closed);

        repository.save(hours);
    }
    public BusinessHours getForDateOrThrow(Long tenantId, LocalDate date) {
        int day = date.getDayOfWeek().getValue(); // 1=lun ... 7=dom
        return repository.findByTenantIdAndDayOfWeek(tenantId, day)
                .orElseThrow(); // luego lo cambias por tu excepción bonita
    }
}
