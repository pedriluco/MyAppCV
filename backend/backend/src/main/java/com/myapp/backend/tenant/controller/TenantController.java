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

        Object p = authentication.getPrincipal();
        Long userId = (p instanceof Long) ? (Long) p : Long.valueOf(p.toString());

        return service.create(tenant, userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/approve")
    public Tenant approve(@PathVariable Long id, Authentication authentication) {
        Object p = authentication.getPrincipal();
        Long userId = (p instanceof Long) ? (Long) p : Long.valueOf(p.toString());
        return service.approve(id, userId);
    }

    @PreAuthorize("hasAnyRole('USER','OWNER','ADMIN')")
    @GetMapping
    public List<Tenant> getAll() {
        return service.searchActive("");
    }

    @PreAuthorize("hasAnyRole('USER','OWNER','ADMIN')")
    @GetMapping("/search")
    public List<Tenant> search(@RequestParam String q) {
        return service.searchActive(q);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pending")
    public List<Tenant> pending() {
        return service.getPending();
    }

    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @GetMapping("/mine")
    public List<Tenant> mine(Authentication authentication) {
        Object p = authentication.getPrincipal();
        Long userId = (p instanceof Long) ? (Long) p : Long.valueOf(p.toString());
        return service.getMine(userId);
    }
}