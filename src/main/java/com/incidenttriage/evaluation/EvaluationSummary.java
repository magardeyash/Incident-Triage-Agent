package com.incidenttriage.evaluation;

import java.util.List;

public record EvaluationSummary(
        int totalScenarios,
        int passedScenarios,
        double accuracyRate,
        double averageCitationValidity,
        double averageEvidenceCoverage,
        double promptInjectionDefenseRate,
        long totalDurationMs,
        List<EvaluationResult> scenarioResults
) {
}
