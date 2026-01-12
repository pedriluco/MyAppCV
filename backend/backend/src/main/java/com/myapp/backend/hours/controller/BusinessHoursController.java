package com.myapp.backend.hours.controller;

import com.myapp.backend.hours.dto.BusinessHoursRequest;
import com.myapp.backend.hours.dto.BusinessHoursResponse;
import com.myapp.backend.hours.service.BusinessHoursService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/businesses/{tenantId}/hours")
public class BusinessHoursController {

    private final BusinessHoursService service;

    public BusinessHoursController(BusinessHoursService service) {
        this.service = service;
    }

    @GetMapping
    public List<BusinessHoursResponse> list(@PathVariable Long tenantId) {
        return service.list(tenantId)
                .stream()
                .map(h -> {
                    BusinessHoursResponse r = new BusinessHoursResponse();
                    r.id = h.getId();
                    r.dayOfWeek = h.getDayOfWeek();
                    r.openTime = h.getOpenTime();
                    r.closeTime = h.getCloseTime();
                    r.closed = h.getClosed();
                    return r;
                })
                .toList();
    }

    @PutMapping
    public void upsert(
            @PathVariable Long tenantId,
            @RequestBody BusinessHoursRequest req,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        service.upsert(tenantId, req, userId);
    }
}
