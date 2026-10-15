package com.incidenttriage.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.incidenttriage.audit.AuditService;
import com.incidenttriage.evidence.EvidenceItem;
import com.incidenttriage.evidence.EvidenceService;
import com.incidenttriage.operationaldata.DependencyService;
import com.incidenttriage.operationaldata.DeploymentService;
import com.incidenttriage.operationaldata.LogService;
import com.incidenttriage.operationaldata.MetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class InvestigationTools {

    private static final Logger log = LoggerFactory.getLogger(InvestigationTools.class);

    private final LogService logService;
    private final MetricsService metricsService;
    private final DeploymentService deploymentService;
    private final DependencyService dependencyService;
    private final EvidenceService evidenceService;
    private final AuditService auditService;
    private final com.incidenttriage.runbook.RunbookSearchService runbookSearchService;
    private final ObjectMapper objectMapper;

    public InvestigationTools(LogService logService,
                              MetricsService metricsService,
                              DeploymentService deploymentService,
                              DependencyService dependencyService,
                              EvidenceService evidenceService,
                              AuditService auditService,
                              com.incidenttriage.runbook.RunbookSearchService runbookSearchService,
                              ObjectMapper objectMapper) {
        this.logService = logService;
        this.metricsService = metricsService;
        this.deploymentService = deploymentService;
        this.dependencyService = dependencyService;
        this.evidenceService = evidenceService;
        this.auditService = auditService;
        this.runbookSearchService = runbookSearchService;
        this.objectMapper = objectMapper;
    }

    public record LogQueryRequest(String service, String startTime, String endTime) {}
    public record MetricQueryRequest(String service, String startTime, String endTime) {}
    public record DeployQueryRequest(String service, String startTime, String endTime) {}
    public record DependencyQueryRequest(String service) {}

    public String queryLogs(String service, String startTimeIso, String endTimeIso) {
        long start = System.currentTimeMillis();
        boolean success = false;
        String errorMessage = null;
        String summary = null;
        Long investigationId = getInvestigationId();

        try {
            validateLimit();
            Instant startInstant = Instant.parse(startTimeIso);
            Instant endInstant = Instant.parse(endTimeIso);

            List<EvidenceItem> evidence = logService.queryLogsAsEvidence(service, startInstant, endInstant);
            if (investigationId != null) {
                evidence.forEach(e -> evidenceService.registerEvidenceItem(investigationId, e));
            }

            success = true;
            summary = "Found " + evidence.size() + " logs for " + service;
            return formatEvidenceItems(evidence);
        } catch (Exception e) {
            errorMessage = e.getMessage();
            log.warn("Tool queryLogs failed: {}", e.getMessage());
            return "ERROR: " + e.getMessage();
        } finally {
            recordAudit(investigationId, "queryLogs", "service=" + service + ", start=" + startTimeIso + ", end=" + endTimeIso,
                    summary != null ? summary : "Failed: " + errorMessage, System.currentTimeMillis() - start, success, errorMessage);
        }
    }

    public String getMetrics(String service, String startTimeIso, String endTimeIso) {
        long start = System.currentTimeMillis();
        boolean success = false;
        String errorMessage = null;
        String summary = null;
        Long investigationId = getInvestigationId();

        try {
            validateLimit();
            Instant startInstant = Instant.parse(startTimeIso);
            Instant endInstant = Instant.parse(endTimeIso);

            List<EvidenceItem> evidence = metricsService.getMetricsAsEvidence(service, startInstant, endInstant);
            if (investigationId != null) {
                evidence.forEach(e -> evidenceService.registerEvidenceItem(investigationId, e));
            }

            success = true;
            summary = "Found " + evidence.size() + " metrics for " + service;
            return formatEvidenceItems(evidence);
        } catch (Exception e) {
            errorMessage = e.getMessage();
            log.warn("Tool getMetrics failed: {}", e.getMessage());
            return "ERROR: " + e.getMessage();
        } finally {
            recordAudit(investigationId, "getMetrics", "service=" + service + ", start=" + startTimeIso + ", end=" + endTimeIso,
                    summary != null ? summary : "Failed: " + errorMessage, System.currentTimeMillis() - start, success, errorMessage);
        }
    }

    public String getRecentDeploys(String service, String startTimeIso, String endTimeIso) {
        long start = System.currentTimeMillis();
        boolean success = false;
        String errorMessage = null;
        String summary = null;
        Long investigationId = getInvestigationId();

        try {
            validateLimit();
            Instant startInstant = Instant.parse(startTimeIso);
            Instant endInstant = Instant.parse(endTimeIso);

            List<EvidenceItem> evidence = deploymentService.getRecentDeploysAsEvidence(service, startInstant, endInstant);
            if (investigationId != null) {
                evidence.forEach(e -> evidenceService.registerEvidenceItem(investigationId, e));
            }

            success = true;
            summary = "Found " + evidence.size() + " deployments for " + service;
            return formatEvidenceItems(evidence);
        } catch (Exception e) {
            errorMessage = e.getMessage();
            log.warn("Tool getRecentDeploys failed: {}", e.getMessage());
            return "ERROR: " + e.getMessage();
        } finally {
            recordAudit(investigationId, "getRecentDeploys", "service=" + service + ", start=" + startTimeIso + ", end=" + endTimeIso,
                    summary != null ? summary : "Failed: " + errorMessage, System.currentTimeMillis() - start, success, errorMessage);
        }
    }

    public String getServiceDependencies(String service) {
        long start = System.currentTimeMillis();
        boolean success = false;
        String errorMessage = null;
        String summary = null;
        Long investigationId = getInvestigationId();

        try {
            validateLimit();
            List<EvidenceItem> evidence = dependencyService.getServiceDependenciesAsEvidence(service);
            if (investigationId != null) {
                evidence.forEach(e -> evidenceService.registerEvidenceItem(investigationId, e));
            }

            success = true;
            summary = "Found " + evidence.size() + " dependencies for " + service;
            return formatEvidenceItems(evidence);
        } catch (Exception e) {
            errorMessage = e.getMessage();
            log.warn("Tool getServiceDependencies failed: {}", e.getMessage());
            return "ERROR: " + e.getMessage();
        } finally {
            recordAudit(investigationId, "getServiceDependencies", "service=" + service,
                    summary != null ? summary : "Failed: " + errorMessage, System.currentTimeMillis() - start, success, errorMessage);
        }
    }

    public String searchRunbooks(String query) {
        long start = System.currentTimeMillis();
        boolean success = false;
        String errorMessage = null;
        String summary = null;
        Long investigationId = getInvestigationId();

        try {
            validateLimit();
            List<EvidenceItem> evidence = runbookSearchService.searchAsEvidence(query, 3, 0.25);
            if (investigationId != null) {
                evidence.forEach(e -> evidenceService.registerEvidenceItem(investigationId, e));
            }

            success = true;
            summary = "Found " + evidence.size() + " runbook chunks for query '" + query + "'";
            return formatEvidenceItems(evidence);
        } catch (Exception e) {
            errorMessage = e.getMessage();
            log.warn("Tool searchRunbooks failed: {}", e.getMessage());
            return "ERROR: " + e.getMessage();
        } finally {
            recordAudit(investigationId, "searchRunbooks", "query=" + query,
                    summary != null ? summary : "Failed: " + errorMessage, System.currentTimeMillis() - start, success, errorMessage);
        }
    }

    private void validateLimit() {
        InvestigationContext context = InvestigationContext.get();
        if (context != null) {
            context.incrementAndValidateToolCalls();
        }
    }

    private Long getInvestigationId() {
        InvestigationContext context = InvestigationContext.get();
        return context != null ? context.getInvestigationId() : 0L;
    }

    private void recordAudit(Long investigationId, String toolName, String sanitizedArgs,
                             String summary, long duration, boolean success, String error) {
        auditService.recordToolCall(investigationId != null ? investigationId : 0L, toolName,
                sanitizedArgs, summary, duration, success, error);
    }

    private String formatEvidenceItems(List<EvidenceItem> items) {
        if (items.isEmpty()) {
            return "No records found.";
        }
        return items.stream()
                .map(item -> "[" + item.id() + "] (" + item.sourceType() + ") " + item.content())
                .collect(Collectors.joining("\n"));
    }
}
