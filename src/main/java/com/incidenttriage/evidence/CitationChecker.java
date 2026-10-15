package com.incidenttriage.evidence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class CitationChecker {

    private static final Logger log = LoggerFactory.getLogger(CitationChecker.class);

    public record CitationValidationResult(
            boolean valid,
            List<String> validCitations,
            List<String> unknownCitations,
            List<String> mismatchedServiceCitations
    ) {}

    public CitationValidationResult validateCitations(Long investigationId, String targetService,
                                                      List<String> citedEvidenceIds,
                                                      Map<String, EvidenceItem> availableEvidence) {
        if (citedEvidenceIds == null || citedEvidenceIds.isEmpty()) {
            return new CitationValidationResult(false, List.of(), List.of("No evidence citations provided"), List.of());
        }

        List<String> validCitations = new ArrayList<>();
        List<String> unknownCitations = new ArrayList<>();
        List<String> mismatchedServiceCitations = new ArrayList<>();

        for (String citedId : citedEvidenceIds) {
            EvidenceItem item = availableEvidence.get(citedId);
            if (item == null) {
                unknownCitations.add(citedId);
                log.warn("Investigation {}: Fabricated or unknown citation detected: {}", investigationId, citedId);
            } else {
                // If it's a service-specific evidence (like LOG, METRIC, DEPLOYMENT), check service match
                if (!"RUNBOOK".equals(item.sourceType()) && !"DEPENDENCY".equals(item.sourceType())
                        && targetService != null && !targetService.equalsIgnoreCase(item.serviceName())) {
                    mismatchedServiceCitations.add(citedId);
                    log.warn("Investigation {}: Citation {} belongs to service '{}', expected '{}'",
                            investigationId, citedId, item.serviceName(), targetService);
                } else {
                    validCitations.add(citedId);
                }
            }
        }

        boolean isValid = unknownCitations.isEmpty() && mismatchedServiceCitations.isEmpty();
        return new CitationValidationResult(isValid, validCitations, unknownCitations, mismatchedServiceCitations);
    }
}
