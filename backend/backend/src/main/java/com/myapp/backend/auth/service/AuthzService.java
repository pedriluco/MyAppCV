package com.myapp.backend.auth.service;

import com.myapp.backend.auth.entity.GlobalRole;
import com.myapp.backend.auth.entity.User;
import com.myapp.backend.auth.repository.UserRepository;
import com.myapp.backend.membership.repository.MembershipRepository;
import org.springframework.stereotype.Service;

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
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    public boolean isAdmin(User user) {
        return user.getGlobalRole() == GlobalRole.ADMIN;
    }

    public void requireTenantAccess(Long businessId, Long userId) {
        User user = requireUser(userId);

        if (user.getGlobalRole() == GlobalRole.ADMIN) return;

        boolean ok = memberships.existsByUserIdAndBusinessId(userId, businessId);
        if (!ok) throw new RuntimeException("Forbidden");
    }

    public void assertAdmin(Long userId) {
        User user = requireUser(userId);
        if (!isAdmin(user)) {
            throw new RuntimeException("Forbidden: requires ADMIN");
        }
    }
}
