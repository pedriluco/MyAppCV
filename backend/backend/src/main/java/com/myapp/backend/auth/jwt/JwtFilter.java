package com.myapp.backend.auth.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().startsWith("/auth/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // DIAGNOSTICO: path y método que llegan al filtro
        System.out.println("PATH=" + request.getServletPath() + " METHOD=" + request.getMethod());

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            Claims claims = jwtService.extractClaims(token);

            Long userId = Long.valueOf(claims.getSubject());
            String role = claims.get("role", String.class);
            if (role == null || role.isBlank()) role = "USER";

            role = role.toUpperCase();
            if (!role.startsWith("ROLE_")) role = "ROLE_" + role;

            System.out.println("JWT role claim=" + claims.get("role", String.class) + " -> authority=" + role);

            var auth = new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    List.of(new SimpleGrantedAuthority(role))
            );

            // DIAGNOSTICO: confirma que sí se setea authentication
            System.out.println("JWT OK userId=" + userId + " authority=" + role + " path=" + request.getServletPath());

            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception e) {
            // DIAGNOSTICO: confirma caída del JWT (exp, firma, etc)
            System.out.println(
                    "JWT FAIL path=" + request.getServletPath() +
                            " err=" + e.getClass().getSimpleName() +
                            ": " + e.getMessage()
            );
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}

