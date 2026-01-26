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

    public void requireTenantAccess(Long tenantId, Long userId) {
        // 1) si no hay userId -> 401 (evita 500 / NPE)
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No auth user");
        }

        // 2) ADMIN bypass (no necesita membership)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin =
                auth != null
                        && auth.isAuthenticated()
                        && auth.getPrincipal() != null
                        && !"anonymousUser".equals(auth.getPrincipal())
                        && auth.getAuthorities().stream()
                        .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        if (isAdmin) {
            return;
        }

        // 3) lógica normal (OWNER + membership)
        User user = requireUser(userId);

        if (user.getGlobalRole() != GlobalRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        boolean ok = memberships.existsByUserIdAndBusinessId(userId, tenantId);
        if (!ok) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    public void assertAdmin(Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No auth");
        }

        boolean ok = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        if (!ok) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin only");
        }
    }
}
