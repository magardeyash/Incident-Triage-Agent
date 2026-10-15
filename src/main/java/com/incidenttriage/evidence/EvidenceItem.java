package com.incidenttriage.evidence;

import java.time.Instant;

public record EvidenceItem(
        String id,
        String sourceType,
        String sourceRecordId,
        String serviceName,
        Instant timestamp,
        String content
) {
    public static EvidenceItem fromLog(String logId, String serviceName, Instant timestamp, String level, String message) {
        return new EvidenceItem(
                "EVID-" + logId,
                "LOG",
                logId,
                serviceName,
                timestamp,
                "[" + level + "] " + message
        );
    }

    public static EvidenceItem fromMetric(String metricId, String serviceName, Instant timestamp, String metricName, Double value, String unit) {
        return new EvidenceItem(
                "EVID-" + metricId,
                "METRIC",
                metricId,
                serviceName,
                timestamp,
                metricName + " = " + value + " " + unit
        );
    }

    public static EvidenceItem fromDeployment(String deployId, String serviceName, Instant timestamp, String version, String status, String summary) {
        return new EvidenceItem(
                "EVID-" + deployId,
                "DEPLOYMENT",
                deployId,
                serviceName,
                timestamp,
                "Deployment " + version + " status: " + status + " (" + summary + ")"
        );
    }

    public static EvidenceItem fromDependency(String depId, String sourceService, String targetService, String type, boolean critical) {
        return new EvidenceItem(
                "EVID-" + depId,
                "DEPENDENCY",
                depId,
                sourceService,
                Instant.now(),
                sourceService + " -> " + targetService + " (" + type + ", critical: " + critical + ")"
        );
    }

    public static EvidenceItem fromRunbookChunk(String chunkId, String documentTitle, String contentSnippet) {
        return new EvidenceItem(
                "EVID-" + chunkId,
                "RUNBOOK",
                chunkId,
                documentTitle,
                Instant.now(),
                contentSnippet
        );
    }
}
