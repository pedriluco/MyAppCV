package com.myapp.backend.tenant.service;

import com.myapp.backend.auth.service.AuthzService;
import com.myapp.backend.membership.entity.Membership;
import com.myapp.backend.membership.entity.MembershipRole;
import com.myapp.backend.membership.entity.MembershipStatus;
import com.myapp.backend.membership.repository.MembershipRepository;
import com.myapp.backend.tenant.entity.Tenant;
import com.myapp.backend.tenant.entity.TenantStatus;
import com.myapp.backend.tenant.repository.TenantRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TenantService {

    private final TenantRepository tenants;
    private final MembershipRepository memberships;
    private final AuthzService authz;

    public TenantService(
            TenantRepository tenants,
            MembershipRepository memberships,
            AuthzService authz
    ) {
        this.tenants = tenants;
        this.memberships = memberships;
        this.authz = authz;
    }

    public List<Tenant> getAll() {
        return tenants.findAll();
    }

    public List<Tenant> getPending() {
        return tenants.findByStatus(TenantStatus.PENDING);
    }

    public Tenant create(Tenant tenant, Long userId) {

        tenant.setStatus(TenantStatus.PENDING);
        Tenant saved = tenants.save(tenant);

        Membership m = memberships.findByBusinessIdAndUserId(saved.getId(), userId)
                .orElseGet(Membership::new);

        m.setBusinessId(saved.getId());
        m.setUserId(userId);
        m.setRole(MembershipRole.OWNER);
        m.setStatus(MembershipStatus.PENDING);

        memberships.save(m);

        return saved;
    }

    public Tenant approve(Long tenantId, Long adminUserId) {

        authz.assertAdmin(adminUserId);

        Tenant t = tenants.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantId));

        t.setStatus(TenantStatus.ACTIVE);
        Tenant saved = tenants.save(t);

        var list = memberships.findAllByBusinessId(tenantId);
        for (Membership m : list) {
            if (m.getRole() == MembershipRole.OWNER) {
                m.setStatus(MembershipStatus.ACTIVE);
                memberships.save(m);
            }
        }

        return saved;
    }

    public List<Tenant> searchActive(String q) {
        String query = (q == null) ? "" : q.trim();
        if (query.isEmpty()) {
            return tenants.findByStatus(TenantStatus.ACTIVE);
        }
        return tenants.findByStatusAndNameContainingIgnoreCase(
                TenantStatus.ACTIVE,
                query
        );
    }
}
