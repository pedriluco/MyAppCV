package com.myapp.backend.auth.controller;

import com.myapp.backend.auth.dto.LoginRequest;
import com.myapp.backend.auth.jwt.JwtService;
import com.myapp.backend.auth.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    private final UserRepository users;
    private final JwtService jwt;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            UserRepository users,
            JwtService jwt,
            PasswordEncoder passwordEncoder
    ) {
        this.users = users;
        this.jwt = jwt;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {

        var userOpt = users.findByEmail(req.getEmail());
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Usuario o contraseña incorrectos");
        }

        var user = userOpt.get();

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Usuario o contraseña incorrectos");
        }

        String token = jwt.generateToken(
                user.getId(),
                user.getGlobalRole().name()
        );

        return ResponseEntity.ok(token);
    }
}
