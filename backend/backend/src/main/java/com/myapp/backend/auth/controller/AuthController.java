package com.myapp.backend.auth.controller;

import com.myapp.backend.auth.dto.AuthResponse;
import com.myapp.backend.auth.dto.LoginRequest;
import com.myapp.backend.auth.dto.RegisterRequest;
import com.myapp.backend.auth.entity.GlobalRole;
import com.myapp.backend.auth.entity.User;
import com.myapp.backend.auth.jwt.JwtService;
import com.myapp.backend.auth.repository.UserRepository;
import com.myapp.backend.tenant.service.TenantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    private final UserRepository users;
    private final JwtService jwt;
    private final PasswordEncoder passwordEncoder;
    private final TenantService tenantService;

    public AuthController(
            UserRepository users,
            JwtService jwt,
            PasswordEncoder passwordEncoder,
            TenantService tenantService
    ) {
        this.users = users;
        this.jwt = jwt;
        this.passwordEncoder = passwordEncoder;
        this.tenantService = tenantService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {

        var user = users.findByEmail(req.getEmail())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos")
                );

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos");
        }

        String token = jwt.generateToken(
                user.getId(),
                user.getGlobalRole().name()
        );

        return ResponseEntity.ok(new AuthResponse(token, null));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest req) {

        if (req.getEmail() == null || req.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email required");
        }

        if (req.getPassword() == null || req.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password required");
        }

        String roleStr = (req.getRole() == null || req.getRole().isBlank())
                ? "USER"
                : req.getRole().trim().toUpperCase();

        if (!roleStr.equals("USER") && !roleStr.equals("OWNER")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "role must be USER or OWNER");
        }

        String email = req.getEmail().trim().toLowerCase();

        if (users.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "email already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setGlobalRole(GlobalRole.valueOf(roleStr));

        users.save(user);

        String token = jwt.generateToken(
                user.getId(),
                user.getGlobalRole().name()
        );

        return ResponseEntity.ok(new AuthResponse(token, null));
    }
}
