package com.incidenttriage.investigation.dto;

import com.incidenttriage.investigation.Investigation;
import com.incidenttriage.investigation.InvestigationStatus;

import java.time.Instant;

public record InvestigationResponse(
        Long id,
        Long alertId,
        InvestigationStatus status,
        Instant startedAt,
        Instant finishedAt,
        String errorMessage,
        Instant createdAt
) {
    public static InvestigationResponse from(Investigation investigation) {
        return new InvestigationResponse(
                investigation.getId(),
                investigation.getAlertId(),
                investigation.getStatus(),
                investigation.getStartedAt(),
                investigation.getFinishedAt(),
                investigation.getErrorMessage(),
                investigation.getCreatedAt()
        );
    }
}
