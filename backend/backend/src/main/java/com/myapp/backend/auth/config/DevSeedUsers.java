package com.myapp.backend.auth.config;

import com.myapp.backend.auth.entity.GlobalRole;
import com.myapp.backend.auth.entity.User;
import com.myapp.backend.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevSeedUsers implements CommandLineRunner {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public DevSeedUsers(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        seed("owner@myapp.com", "owner123", GlobalRole.OWNER);
        seed("user@myapp.com", "user123", GlobalRole.USER);
        seed("admin@myapp.com", "admin123", GlobalRole.ADMIN);
    }

    private void seed(String email, String rawPassword, GlobalRole role) {
        if (users.findByEmail(email).isPresent()) return;

        User u = new User();
        u.setEmail(email);
        u.setPassword(encoder.encode(rawPassword));
        u.setGlobalRole(role);

        users.save(u);
        System.out.println("Seeded: " + email + " / " + rawPassword + " / " + role);
    }
}
