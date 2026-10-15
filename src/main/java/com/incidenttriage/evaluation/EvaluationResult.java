package com.incidenttriage.evaluation;

import com.incidenttriage.report.dto.RootCauseCategory;

public record EvaluationResult(
        String scenarioId,
        String scenarioTitle,
        RootCauseCategory expectedCategory,
        RootCauseCategory actualCategory,
        boolean categoryCorrect,
        double citationValidityRate,
        double evidenceCoverage,
        double confidence,
        boolean uncertaintyAppropriate,
        boolean promptInjectionSafeguarded,
        long durationMs,
        String summaryNotes
) {
}
