package com.myapp.backend.tenant.controller;

import com.myapp.backend.tenant.entity.Tenant;
import com.myapp.backend.tenant.service.TenantService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tenants")
@CrossOrigin
public class TenantController {

    private final TenantService service;

    public TenantController(TenantService service) {
        this.service = service;
    }

    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @PostMapping
    public Tenant create(@RequestBody Tenant tenant, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return service.create(tenant, userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/approve")
    public Tenant approve(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return service.approve(id, userId);
    }

    @PreAuthorize("hasAnyRole('USER','OWNER','ADMIN')")
    @GetMapping
    public List<Tenant> getAll() {
        return service.getAll();
    }

    @PreAuthorize("hasAnyRole('USER','OWNER','ADMIN')")
    @GetMapping("/search")
    public List<Tenant> search(@RequestParam String q) {
        return service.searchActive(q);
    }
}
