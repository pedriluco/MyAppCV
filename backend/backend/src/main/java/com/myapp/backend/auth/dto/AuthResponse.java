package com.myapp.backend.auth.dto;

public class AuthResponse {

    private String token;
    private Long tenantId;

    public AuthResponse(String token, Long tenantId) {
        this.token = token;
        this.tenantId = tenantId;
    }

    public String getToken() {
        return token;
    }

    public Long getTenantId() {
        return tenantId;
    }
}
