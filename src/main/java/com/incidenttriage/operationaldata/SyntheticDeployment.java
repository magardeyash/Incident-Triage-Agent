package com.incidenttriage.operationaldata;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "synthetic_deployments")
public class SyntheticDeployment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "deployment_id", unique = true, nullable = false)
    private String deploymentId;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(name = "version", nullable = false)
    private String version;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "deployed_at", nullable = false)
    private Instant deployedAt;

    @Column(name = "commit_hash", nullable = false)
    private String commitHash;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    public SyntheticDeployment() {
    }

    public SyntheticDeployment(String deploymentId, String serviceName, String version, String status, Instant deployedAt, String commitHash, String summary) {
        this.deploymentId = deploymentId;
        this.serviceName = serviceName;
        this.version = version;
        this.status = status;
        this.deployedAt = deployedAt;
        this.commitHash = commitHash;
        this.summary = summary;
    }

    public Long getId() {
        return id;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getVersion() {
        return version;
    }

    public String getStatus() {
        return status;
    }

    public Instant getDeployedAt() {
        return deployedAt;
    }

    public String getCommitHash() {
        return commitHash;
    }

    public String getSummary() {
        return summary;
    }
}
