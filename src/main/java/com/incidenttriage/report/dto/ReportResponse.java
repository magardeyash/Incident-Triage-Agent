package com.incidenttriage.report.dto;

import com.incidenttriage.report.InvestigationReport;

import java.time.Instant;

public record ReportResponse(
        Long id,
        Long investigationId,
        String summary,
        RootCauseCategory probableRootCause,
        Double confidence,
        String evidence,
        String recommendations,
        String limitations,
        String modelName,
        String promptVersion,
        Instant createdAt
) {
    public static ReportResponse from(InvestigationReport report) {
        return new ReportResponse(
                report.getId(),
                report.getInvestigationId(),
                report.getSummary(),
                report.getProbableRootCause(),
                report.getConfidence(),
                report.getEvidence(),
                report.getRecommendations(),
                report.getLimitations(),
                report.getModelName(),
                report.getPromptVersion(),
                report.getCreatedAt()
        );
    }
}
