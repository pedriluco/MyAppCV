package com.myapp.backend.controller;

import com.myapp.backend.entity.Tenant;
import com.myapp.backend.repository.TenantRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tenants")
@CrossOrigin
public class TenantController {

    private final TenantRepository repository;

    public TenantController(TenantRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public Tenant create(@RequestBody Tenant tenant) {
        return repository.save(tenant);
    }

    @GetMapping
    public List<Tenant> getAll() {
        return repository.findAll();
    }

    @GetMapping("/search")
    public List<Tenant> search(@RequestParam String q) {
        return repository.findByNameContainingIgnoreCase(q);
    }
}
