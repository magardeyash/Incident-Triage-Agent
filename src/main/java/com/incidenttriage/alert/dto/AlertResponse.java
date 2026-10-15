package com.incidenttriage.alert.dto;

import com.incidenttriage.alert.Alert;

import java.time.Instant;

public record AlertResponse(
        Long id,
        String serviceName,
        String severity,
        String message,
        Instant occurredAt,
        String payload,
        Instant createdAt
) {
    public static AlertResponse from(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getServiceName(),
                alert.getSeverity(),
                alert.getMessage(),
                alert.getOccurredAt(),
                alert.getPayload(),
                alert.getCreatedAt()
        );
    }
}
