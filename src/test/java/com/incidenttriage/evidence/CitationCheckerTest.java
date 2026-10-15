package com.incidenttriage.evidence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CitationCheckerTest {

    private final CitationChecker checker = new CitationChecker();

    @Test
    @DisplayName("Should validate valid citations matching service and catalog")
    void testValidCitations() {
        EvidenceItem item1 = EvidenceItem.fromLog("chk-001", "checkout-service", Instant.now(), "ERROR", "NullPointer");
        EvidenceItem item2 = EvidenceItem.fromDeployment("chk-dep-1", "checkout-service", Instant.now(), "v1.4.2", "SUCCESS", "New cart");

        Map<String, EvidenceItem> available = Map.of(
                item1.id(), item1,
                item2.id(), item2
        );

        CitationChecker.CitationValidationResult result = checker.validateCitations(
                101L, "checkout-service", List.of(item1.id(), item2.id()), available);

        assertThat(result.valid()).isTrue();
        assertThat(result.unknownCitations()).isEmpty();
        assertThat(result.mismatchedServiceCitations()).isEmpty();
    }

    @Test
    @DisplayName("Should detect unknown and fabricated citations")
    void testUnknownCitations() {
        EvidenceItem item1 = EvidenceItem.fromLog("chk-001", "checkout-service", Instant.now(), "ERROR", "NullPointer");
        Map<String, EvidenceItem> available = Map.of(item1.id(), item1);

        CitationChecker.CitationValidationResult result = checker.validateCitations(
                101L, "checkout-service", List.of(item1.id(), "EVID-FAKE-999"), available);

        assertThat(result.valid()).isFalse();
        assertThat(result.unknownCitations()).contains("EVID-FAKE-999");
    }

    @Test
    @DisplayName("Should flag citations from an unrelated service")
    void testMismatchedServiceCitations() {
        EvidenceItem paymentItem = EvidenceItem.fromLog("pay-001", "payment-service", Instant.now(), "ERROR", "Stripe timeout");
        Map<String, EvidenceItem> available = Map.of(paymentItem.id(), paymentItem);

        CitationChecker.CitationValidationResult result = checker.validateCitations(
                101L, "checkout-service", List.of(paymentItem.id()), available);

        assertThat(result.valid()).isFalse();
        assertThat(result.mismatchedServiceCitations()).contains(paymentItem.id());
    }
}
