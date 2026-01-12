import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TenantService {

    private final TenantRepository repo;
    private final MembershipService membershipService;
    private final AuthzService authz;

    public TenantService(
            TenantRepository repo,
            MembershipService membershipService,
            AuthzService authz
    ) {
        this.repo = repo;
        this.membershipService = membershipService;
        this.authz = authz;
    }

    public Tenant create(Tenant tenant, Long userId) {
        tenant.setStatus("PENDING");
        Tenant saved = repo.save(tenant);

        // crear OWNER
        membershipService.createOwnerMembership(saved.getId(), userId);

        return saved;
    }

    public Tenant approve(Long tenantId, Long userId) {
        authz.assertAdmin(userId);

        Tenant tenant = repo.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        tenant.setStatus("ACTIVE");
        return repo.save(tenant);
    }

    public List<Tenant> getAll() {
        return repo.findAll();
    }

    public List<Tenant> searchActive(String q) {
        return repo.findByNameContainingIgnoreCaseAndStatus(q, "ACTIVE");
    }
}
