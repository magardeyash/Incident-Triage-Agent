package com.incidenttriage.operationaldata;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "synthetic_logs")
public class SyntheticLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "log_id", unique = true, nullable = false)
    private String logId;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "level", nullable = false)
    private String level;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    public SyntheticLog() {
    }

    public SyntheticLog(String logId, String serviceName, Instant timestamp, String level, String message, String metadata) {
        this.logId = logId;
        this.serviceName = serviceName;
        this.timestamp = timestamp;
        this.level = level;
        this.message = message;
        this.metadata = metadata;
    }

    public Long getId() {
        return id;
    }

    public String getLogId() {
        return logId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public String getMetadata() {
        return metadata;
    }
}
