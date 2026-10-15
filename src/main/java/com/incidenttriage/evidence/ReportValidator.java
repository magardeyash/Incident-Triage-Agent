package com.incidenttriage.evidence;

import com.incidenttriage.report.dto.IncidentReportDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ReportValidator {

    private final CitationChecker citationChecker;

    public record ReportValidationResult(
            boolean valid,
            List<String> errors,
            CitationChecker.CitationValidationResult citationResult
    ) {}

    public ReportValidator(CitationChecker citationChecker) {
        this.citationChecker = citationChecker;
    }

    public ReportValidationResult validateReport(Long investigationId, String targetService,
                                                 IncidentReportDto reportDto,
                                                 Map<String, EvidenceItem> availableEvidence) {
        List<String> errors = new ArrayList<>();

        if (reportDto == null) {
            return new ReportValidationResult(false, List.of("Report DTO is null"), null);
        }

        if (reportDto.summary() == null || reportDto.summary().isBlank()) {
            errors.add("Report summary must not be blank");
        }

        if (reportDto.probableRootCause() == null) {
            errors.add("Probable root cause category must not be null");
        }

        if (reportDto.confidence() == null || reportDto.confidence() < 0.0 || reportDto.confidence() > 1.0) {
            errors.add("Confidence must be between 0.0 and 1.0");
        }

        if (reportDto.evidence() == null || reportDto.evidence().isEmpty()) {
            errors.add("Report must contain at least one evidence reference");
        }

        if (reportDto.recommendations() == null || reportDto.recommendations().isEmpty()) {
            errors.add("Report must contain at least one recommendation");
        }

        // Validate citations
        CitationChecker.CitationValidationResult citationResult = null;
        if (reportDto.evidence() != null && !reportDto.evidence().isEmpty()) {
            citationResult = citationChecker.validateCitations(
                    investigationId, targetService, reportDto.evidence(), availableEvidence);
            if (!citationResult.valid()) {
                if (!citationResult.unknownCitations().isEmpty()) {
                    errors.add("Unknown or fabricated citations: " + citationResult.unknownCitations());
                }
                if (!citationResult.mismatchedServiceCitations().isEmpty()) {
                    errors.add("Citations from unrelated services: " + citationResult.mismatchedServiceCitations());
                }
            }
        }

        return new ReportValidationResult(errors.isEmpty(), errors, citationResult);
    }
}
