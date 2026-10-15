package com.incidenttriage.evaluation;

import com.incidenttriage.report.dto.RootCauseCategory;

import java.time.Instant;
import java.util.List;

public record EvaluationScenario(
        String id,
        String title,
        String service,
        String severity,
        String message,
        Instant occurredAt,
        RootCauseCategory expectedRootCause,
        List<String> expectedEvidenceSubstrings,
        boolean expectUncertainty,
        boolean containsInjection
) {
}
