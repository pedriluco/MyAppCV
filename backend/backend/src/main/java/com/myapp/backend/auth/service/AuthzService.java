package com.myapp.backend.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import java.security.Key;

@Service
public class AuthzService {

    private final Key key;

    public AuthzService(Key key) {
        this.key = key;
    }

    public void assertAdmin(String jwt) {
        Claims c = parse(jwt);
        String role = c.get("role", String.class);
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("Forbidden");
        }
    }

    public void assertOwnerOrAdmin(String jwt, Long businessId) {
        Claims c = parse(jwt);
        String role = c.get("role", String.class);
        if ("ADMIN".equals(role) || "OWNER".equals(role)) return;
        throw new RuntimeException("Forbidden");
    }

    public Long getUserId(String jwt) {
        return Long.parseLong(parse(jwt).getSubject());
    }

    public String getRole(String jwt) {
        return parse(jwt).get("role", String.class);
    }

    private Claims parse(String jwt) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(jwt)
                .getBody();
    }
}
