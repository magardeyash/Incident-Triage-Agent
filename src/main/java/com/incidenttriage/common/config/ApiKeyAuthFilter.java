package com.incidenttriage.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.incidenttriage.common.exception.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final String expectedApiKey;
    private final String headerName;
    private final ObjectMapper objectMapper;

    public ApiKeyAuthFilter(
            @Value("${app.security.api-key:demo-test-api-key}") String expectedApiKey,
            @Value("${app.security.header-name:X-API-Key}") String headerName,
            ObjectMapper objectMapper) {
        this.expectedApiKey = expectedApiKey;
        this.headerName = headerName;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Bypass preflight CORS OPTIONS requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Only enforce API key authentication on /api/ endpoints
        if (path.startsWith("/api/")) {
            String providedKey = request.getHeader(headerName);
            if (providedKey == null || !providedKey.equals(expectedApiKey)) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                ErrorResponse errorResponse = ErrorResponse.of(
                        HttpStatus.UNAUTHORIZED.value(),
                        "Unauthorized",
                        "Missing or invalid API key",
                        path
                );
                response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
