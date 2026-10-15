package com.incidenttriage.alert.dto;

import com.incidenttriage.investigation.InvestigationStatus;

public record CreateAlertResponse(
        Long alertId,
        Long investigationId,
        InvestigationStatus status
) {
}
