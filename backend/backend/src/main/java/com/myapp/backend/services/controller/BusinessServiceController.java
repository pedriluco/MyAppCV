package com.myapp.backend.services.controller;

import com.myapp.backend.services.dto.CreateServiceRequest;
import com.myapp.backend.services.dto.ServiceResponse;
import com.myapp.backend.services.service.BusinessServiceService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/businesses/{tenantId}/services")
@CrossOrigin
public class BusinessServiceController {

    private final BusinessServiceService service;

    public BusinessServiceController(BusinessServiceService service) {
        this.service = service;
    }

    @GetMapping
    public List<ServiceResponse> list(@PathVariable Long tenantId) {
        return service.listActive(tenantId)
                .stream()
                .map(s -> new ServiceResponse(
                        s.getId(),
                        s.getTenantId(),
                        s.getName(),
                        s.getDurationMinutes(),
                        s.getActive()
                ))
                .toList();
    }

    @PostMapping
    public ServiceResponse create(
            @PathVariable Long tenantId,
            @RequestBody CreateServiceRequest req,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        var saved = service.create(tenantId, req, userId);

        return new ServiceResponse(
                saved.getId(),
                saved.getTenantId(),
                saved.getName(),
                saved.getDurationMinutes(),
                saved.getActive()
        );
    }

    @PatchMapping("/{serviceId}/active")
    public void setActive(
            @PathVariable Long tenantId,
            @PathVariable Long serviceId,
            @RequestParam Boolean active,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        service.setActive(tenantId, serviceId, active, userId);
    }
}
