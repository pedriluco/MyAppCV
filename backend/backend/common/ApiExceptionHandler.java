package com.myapp.backend.common;

import jakarta.servlet.http.HttpServletRequest;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handle(
            ResponseStatusException ex,
            HttpServletRequest request
    ) {
        if (ex.getStatusCode().value() == 403 && log.isDebugEnabled()) {
            String auth = request.getHeader("Authorization");
            log.debug("403 Forbidden -> {} {}", request.getMethod(), request.getRequestURI());
            log.debug("JWT payload:\n{}", decodeJwtPayload(auth));
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", ex.getStatusCode().value());
        body.put("error", ex.getStatusCode().toString());
        body.put("message", ex.getReason());

        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    private String decodeJwtPayload(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return "NO_TOKEN";
        }

        try {
            String token = authorization.substring(7);
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return "NOT_JWT";
            }

            byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
            String json = new String(decoded, StandardCharsets.UTF_8);
            return new JSONObject(json).toString(2);
        } catch (Exception e) {
            return "PAYLOAD_DECODE_ERROR: " + e.getMessage();
        }
    }
}
