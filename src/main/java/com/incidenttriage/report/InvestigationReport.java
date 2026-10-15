package com.incidenttriage.report;

import com.incidenttriage.report.dto.RootCauseCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "investigation_reports")
public class InvestigationReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "investigation_id", nullable = false)
    private Long investigationId;

    @Column(name = "summary", nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(name = "probable_root_cause", nullable = false)
    private RootCauseCategory probableRootCause;

    @Column(name = "confidence", nullable = false)
    private Double confidence;

    @Column(name = "evidence", nullable = false, columnDefinition = "TEXT")
    private String evidence;

    @Column(name = "recommendations", nullable = false, columnDefinition = "TEXT")
    private String recommendations;

    @Column(name = "limitations", columnDefinition = "TEXT")
    private String limitations;

    @Column(name = "model_name", nullable = false)
    private String modelName;

    @Column(name = "prompt_version", nullable = false)
    private String promptVersion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public InvestigationReport() {
    }

    public InvestigationReport(Long investigationId, String summary, RootCauseCategory probableRootCause,
                               Double confidence, String evidence, String recommendations,
                               String limitations, String modelName, String promptVersion) {
        this.investigationId = investigationId;
        this.summary = summary;
        this.probableRootCause = probableRootCause;
        this.confidence = confidence;
        this.evidence = evidence;
        this.recommendations = recommendations;
        this.limitations = limitations;
        this.modelName = modelName;
        this.promptVersion = promptVersion;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInvestigationId() {
        return investigationId;
    }

    public void setInvestigationId(Long investigationId) {
        this.investigationId = investigationId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public RootCauseCategory getProbableRootCause() {
        return probableRootCause;
    }

    public void setProbableRootCause(RootCauseCategory probableRootCause) {
        this.probableRootCause = probableRootCause;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public String getEvidence() {
        return evidence;
    }

    public void setEvidence(String evidence) {
        this.evidence = evidence;
    }

    public String getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }

    public String getLimitations() {
        return limitations;
    }

    public void setLimitations(String limitations) {
        this.limitations = limitations;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getPromptVersion() {
        return promptVersion;
    }

    public void setPromptVersion(String promptVersion) {
        this.promptVersion = promptVersion;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
