package com.myapp.backend.auth.service;

import com.myapp.backend.auth.entity.GlobalRole;
import com.myapp.backend.auth.entity.User;
import com.myapp.backend.auth.repository.UserRepository;
import com.myapp.backend.membership.repository.MembershipRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthzService {

    private final UserRepository users;
    private final MembershipRepository memberships;

    public AuthzService(UserRepository users, MembershipRepository memberships) {
        this.users = users;
        this.memberships = memberships;
    }

    public User requireUser(Long userId) {
        return users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private boolean isAdmin(Authentication auth) {
        // LOG temporal (quítalo después)
        System.out.println("AUTH=" + auth);
        System.out.println("AUTH authorities=" + (auth != null ? auth.getAuthorities() : null));

        return auth != null
                && auth.isAuthenticated()
                && auth.getAuthorities() != null
                && auth.getAuthorities().stream().anyMatch(a ->
                "ROLE_ADMIN".equals(a.getAuthority())
                        || "ADMIN".equals(a.getAuthority())
                        || "SCOPE_ADMIN".equals(a.getAuthority())
        );
    }

    public void requireTenantAccess(Long tenantId, Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No auth user");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("AUTHZ requireTenantAccess tenantId=" + tenantId + " userId=" + userId
                + " auth=" + (auth != null ? auth.getAuthorities() : null));

        if (isAdmin(auth)) {
            System.out.println("AUTHZ ADMIN BYPASS ✅");
            return;
        }

        User user = requireUser(userId);

        if (user.getGlobalRole() != GlobalRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        boolean ok = memberships.existsByUserIdAndBusinessId(userId, tenantId);
        if (!ok) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }
    }

    public void assertAdmin(Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getAuthorities() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No auth");
        }

        if (!isAdmin(auth)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin only");
        }
    }
}

