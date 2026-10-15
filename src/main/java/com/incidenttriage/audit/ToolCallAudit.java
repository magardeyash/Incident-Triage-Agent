package com.incidenttriage.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "tool_call_audits")
public class ToolCallAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "investigation_id", nullable = false)
    private Long investigationId;

    @Column(name = "tool_name", nullable = false)
    private String toolName;

    @Column(name = "sanitized_arguments", columnDefinition = "TEXT")
    private String sanitizedArguments;

    @Column(name = "result_summary", columnDefinition = "TEXT")
    private String resultSummary;

    @Column(name = "duration_ms", nullable = false)
    private Long durationMs;

    @Column(name = "success", nullable = false)
    private boolean success;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public ToolCallAudit() {
    }

    public ToolCallAudit(Long investigationId, String toolName, String sanitizedArguments,
                         String resultSummary, Long durationMs, boolean success, String errorMessage) {
        this.investigationId = investigationId;
        this.toolName = toolName;
        this.sanitizedArguments = sanitizedArguments;
        this.resultSummary = resultSummary;
        this.durationMs = durationMs;
        this.success = success;
        this.errorMessage = errorMessage;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getInvestigationId() {
        return investigationId;
    }

    public String getToolName() {
        return toolName;
    }

    public String getSanitizedArguments() {
        return sanitizedArguments;
    }

    public String getResultSummary() {
        return resultSummary;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
