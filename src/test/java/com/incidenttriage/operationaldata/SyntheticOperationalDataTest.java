package com.incidenttriage.operationaldata;

import com.incidenttriage.alert.Alert;
import com.incidenttriage.evidence.EvidenceItem;
import com.incidenttriage.evidence.EvidenceService;
import com.incidenttriage.report.InvestigationReport;
import com.incidenttriage.report.dto.RootCauseCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class SyntheticOperationalDataTest {

    @Autowired
    private LogService logService;

    @Autowired
    private MetricsService metricsService;

    @Autowired
    private DeploymentService deploymentService;

    @Autowired
    private DependencyService dependencyService;

    @Autowired
    private EvidenceService evidenceService;

    @Autowired
    private SyntheticLogRepository logRepo;

    @Autowired
    private SyntheticMetricRepository metricRepo;

    @Autowired
    private SyntheticDeploymentRepository deployRepo;

    @Autowired
    private SyntheticDependencyRepository depRepo;

    @BeforeEach
    void setupSeedData() {
        if (logRepo.count() == 0) {
            logRepo.save(new SyntheticLog("LOG-chk-001", "checkout-service", Instant.parse("2026-10-15T10:14:50Z"), "INFO", "Started checkout", "{}"));
            logRepo.save(new SyntheticLog("LOG-chk-003", "checkout-service", Instant.parse("2026-10-15T10:17:12Z"), "ERROR", "NullPointerException: discountService is null", "{}"));
        }
        if (metricRepo.count() == 0) {
            metricRepo.save(new SyntheticMetric("MET-chk-err-1020", "checkout-service", "error_rate", 0.18, "ratio", Instant.parse("2026-10-15T10:20:00Z")));
        }
        if (deployRepo.count() == 0) {
            deployRepo.save(new SyntheticDeployment("DEP-chk-v1.4.2", "checkout-service", "v1.4.2", "SUCCESS", Instant.parse("2026-10-15T10:15:00Z"), "c8f39a1", "Cart discounts module"));
        }
        if (depRepo.count() == 0) {
            depRepo.save(new SyntheticServiceDependency("DEPGRAPH-chk-pay", "checkout-service", "payment-service", "SYNC_HTTP", true));
        }
    }

    @Test
    @DisplayName("Should query logs by service and valid time window")
    void testQueryLogs() {
        Instant start = Instant.parse("2026-10-15T10:00:00Z");
        Instant end = Instant.parse("2026-10-15T10:30:00Z");

        List<SyntheticLog> logs = logService.queryLogs("checkout-service", start, end);
        assertThat(logs).isNotEmpty();
        assertThat(logs.stream().anyMatch(l -> l.getLogId().equals("LOG-chk-003"))).isTrue();
    }

    @Test
    @DisplayName("Should reject invalid time window (>24h or reversed)")
    void testInvalidTimeWindow() {
        Instant now = Instant.now();
        Instant past = now.minus(Duration.ofHours(30));

        assertThatThrownBy(() -> logService.queryLogs("checkout-service", now, past))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> logService.queryLogs("checkout-service", past, now))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should query metrics by service and window")
    void testQueryMetrics() {
        Instant start = Instant.parse("2026-10-15T10:00:00Z");
        Instant end = Instant.parse("2026-10-15T10:30:00Z");

        List<SyntheticMetric> metrics = metricsService.getMetrics("checkout-service", start, end);
        assertThat(metrics).isNotEmpty();
        assertThat(metrics.get(0).getMetricName()).isEqualTo("error_rate");
    }

    @Test
    @DisplayName("Should query dependencies for service")
    void testQueryDependencies() {
        List<SyntheticServiceDependency> deps = dependencyService.getServiceDependencies("checkout-service");
        assertThat(deps).isNotEmpty();
        assertThat(deps.get(0).getTargetService()).isEqualTo("payment-service");
    }

    @Test
    @DisplayName("Should collect stable evidence and generate deterministic baseline report")
    void testDeterministicInvestigation() {
        Alert alert = new Alert(
                "checkout-service",
                "HIGH",
                "Error rate spike > 15%",
                Instant.parse("2026-10-15T10:20:00Z"),
                null
        );

        Long testInvId = 999L;
        List<EvidenceItem> evidence = evidenceService.collectEvidenceForAlert(testInvId, alert);
        assertThat(evidence).isNotEmpty();

        // Check stable IDs
        assertThat(evidence.stream().allMatch(e -> e.id().startsWith("EVID-"))).isTrue();

        // Check catalog registration
        assertThat(evidenceService.getEvidenceForInvestigation(testInvId)).isNotEmpty();

        // Generate deterministic baseline report
        InvestigationReport report = evidenceService.generateDeterministicReport(testInvId, alert, evidence);
        assertThat(report.getProbableRootCause()).isEqualTo(RootCauseCategory.BAD_DEPLOY);
        assertThat(report.getConfidence()).isGreaterThanOrEqualTo(0.8);
        assertThat(report.getModelName()).isEqualTo("deterministic-baseline-engine");
        assertThat(report.getSummary()).contains("v1.4.2");
    }
}
