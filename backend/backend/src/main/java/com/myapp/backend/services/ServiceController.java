package com.myapp.backend.services;

import com.myapp.backend.services.dto.CreateServiceRequest;
import com.myapp.backend.services.dto.ServiceResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/services")
public class ServiceController {

    private final ServiceRepository repository;

    public ServiceController(ServiceRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ServiceResponse create(@RequestBody CreateServiceRequest req) {
        Service s = new Service();
        s.setTenantId(req.tenantId());
        s.setName(req.name());
        s.setDurationMinutes(req.durationMinutes());

        Service saved = repository.save(s);

        return new ServiceResponse(
                saved.getId(),
                saved.getTenantId(),
                saved.getName(),
                saved.getDurationMinutes()
        );
    }

    @GetMapping
    public List<ServiceResponse> getByTenant(@RequestParam Long tenantId) {
        return repository.findByTenantId(tenantId)
                .stream()
                .map(s -> new ServiceResponse(
                        s.getId(),
                        s.getTenantId(),
                        s.getName(),
                        s.getDurationMinutes()
                ))
                .toList();
    }
}
