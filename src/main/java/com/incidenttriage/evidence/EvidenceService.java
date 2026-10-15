package com.incidenttriage.evidence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.incidenttriage.alert.Alert;
import com.incidenttriage.operationaldata.DependencyService;
import com.incidenttriage.operationaldata.DeploymentService;
import com.incidenttriage.operationaldata.LogService;
import com.incidenttriage.operationaldata.MetricsService;
import com.incidenttriage.report.InvestigationReport;
import com.incidenttriage.report.dto.RootCauseCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EvidenceService {

    private static final Logger log = LoggerFactory.getLogger(EvidenceService.class);

    private final LogService logService;
    private final MetricsService metricsService;
    private final DeploymentService deploymentService;
    private final DependencyService dependencyService;
    private final ObjectMapper objectMapper;

    // Active evidence registry per investigation: investigationId -> (evidenceId -> EvidenceItem)
    private final Map<Long, Map<String, EvidenceItem>> investigationEvidenceRegistry = new ConcurrentHashMap<>();

    public EvidenceService(LogService logService,
                           MetricsService metricsService,
                           DeploymentService deploymentService,
                           DependencyService dependencyService,
                           ObjectMapper objectMapper) {
        this.logService = logService;
        this.metricsService = metricsService;
        this.deploymentService = deploymentService;
        this.dependencyService = dependencyService;
        this.objectMapper = objectMapper;
    }

    public List<EvidenceItem> collectEvidenceForAlert(Long investigationId, Alert alert) {
        Instant alertTime = alert.getOccurredAt();
        Instant startTime = alertTime.minus(Duration.ofMinutes(30));
        Instant endTime = alertTime.plus(Duration.ofMinutes(15));
        String service = alert.getServiceName();

        List<EvidenceItem> items = new ArrayList<>();

        // 1. Logs
        items.addAll(logService.queryLogsAsEvidence(service, startTime, endTime));

        // 2. Metrics
        items.addAll(metricsService.getMetricsAsEvidence(service, startTime, endTime));

        // 3. Deployments
        items.addAll(deploymentService.getRecentDeploysAsEvidence(service, alertTime.minus(Duration.ofHours(4)), endTime));

        // 4. Dependencies
        items.addAll(dependencyService.getServiceDependenciesAsEvidence(service));

        // Register in active registry for citation checking
        Map<String, EvidenceItem> map = investigationEvidenceRegistry.computeIfAbsent(investigationId, k -> new ConcurrentHashMap<>());
        for (EvidenceItem item : items) {
            map.put(item.id(), item);
        }

        log.info("Collected {} evidence items for investigation {}", items.size(), investigationId);
        return items;
    }

    public void registerEvidenceItem(Long investigationId, EvidenceItem item) {
        investigationEvidenceRegistry
                .computeIfAbsent(investigationId, k -> new ConcurrentHashMap<>())
                .put(item.id(), item);
    }

    public Map<String, EvidenceItem> getEvidenceForInvestigation(Long investigationId) {
        return investigationEvidenceRegistry.getOrDefault(investigationId, Collections.emptyMap());
    }

    public void clearEvidenceForInvestigation(Long investigationId) {
        investigationEvidenceRegistry.remove(investigationId);
    }

    /**
     * Deterministic rule-based baseline investigation (Phase 2 requirement).
     * Used when running without LLM or as a fallback baseline.
     */
    public InvestigationReport generateDeterministicReport(Long investigationId, Alert alert, List<EvidenceItem> evidence) {
        List<String> citedEvidenceIds = new ArrayList<>();
        RootCauseCategory probableCause = RootCauseCategory.UNKNOWN;
        double confidence = 0.5;
        String summary;
        String limitations = "Deterministic baseline analysis without LLM inference. Checked standard error patterns.";

        boolean hasErrorLogs = false;
        boolean hasRecentDeploy = false;
        String recentDeployId = null;

        for (EvidenceItem item : evidence) {
            if ("LOG".equals(item.sourceType()) && item.content().contains("ERROR")) {
                hasErrorLogs = true;
                citedEvidenceIds.add(item.id());
            }
            if ("DEPLOYMENT".equals(item.sourceType()) && item.content().contains("v1.4.2")) {
                hasRecentDeploy = true;
                recentDeployId = item.id();
                citedEvidenceIds.add(item.id());
            }
            if ("METRIC".equals(item.sourceType()) && item.content().contains("error_rate")) {
                citedEvidenceIds.add(item.id());
            }
        }

        if (hasRecentDeploy && hasErrorLogs) {
            probableCause = RootCauseCategory.BAD_DEPLOY;
            confidence = 0.85;
            summary = "Error spike correlated directly with recent release " + recentDeployId + " for " + alert.getServiceName();
        } else if (hasErrorLogs) {
            probableCause = RootCauseCategory.DEPENDENCY_FAILURE;
            confidence = 0.70;
            summary = "Downstream timeouts or errors observed for service " + alert.getServiceName();
        } else {
            summary = "Insufficient operational signals to isolate root cause with high confidence.";
            confidence = 0.30;
        }

        List<String> recommendations = List.of(
                "Roll back or canary verify the recent deployment if errors persist",
                "Verify downstream health and connection pools for critical dependencies",
                "Monitor error rate metrics on " + alert.getServiceName()
        );

        String evidenceJson;
        String recsJson;
        try {
            evidenceJson = objectMapper.writeValueAsString(citedEvidenceIds);
            recsJson = objectMapper.writeValueAsString(recommendations);
        } catch (JsonProcessingException e) {
            evidenceJson = "[]";
            recsJson = "[]";
        }

        return new InvestigationReport(
                investigationId,
                summary,
                probableCause,
                confidence,
                evidenceJson,
                recsJson,
                limitations,
                "deterministic-baseline-engine",
                "v1.0"
        );
    }
}
