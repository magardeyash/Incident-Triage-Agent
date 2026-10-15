package com.incidenttriage.operationaldata;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "synthetic_service_dependencies")
public class SyntheticServiceDependency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dependency_id", unique = true, nullable = false)
    private String dependencyId;

    @Column(name = "source_service", nullable = false)
    private String sourceService;

    @Column(name = "target_service", nullable = false)
    private String targetService;

    @Column(name = "dependency_type", nullable = false)
    private String dependencyType;

    @Column(name = "critical", nullable = false)
    private boolean critical = true;

    public SyntheticServiceDependency() {
    }

    public SyntheticServiceDependency(String dependencyId, String sourceService, String targetService, String dependencyType, boolean critical) {
        this.dependencyId = dependencyId;
        this.sourceService = sourceService;
        this.targetService = targetService;
        this.dependencyType = dependencyType;
        this.critical = critical;
    }

    public Long getId() {
        return id;
    }

    public String getDependencyId() {
        return dependencyId;
    }

    public String getSourceService() {
        return sourceService;
    }

    public String getTargetService() {
        return targetService;
    }

    public String getDependencyType() {
        return dependencyType;
    }

    public boolean isCritical() {
        return critical;
    }
}
