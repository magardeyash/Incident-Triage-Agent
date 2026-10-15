package com.incidenttriage.operationaldata;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "synthetic_metrics")
public class SyntheticMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "metric_id", unique = true, nullable = false)
    private String metricId;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(name = "metric_name", nullable = false)
    private String metricName;

    @Column(name = "value", nullable = false)
    private Double value;

    @Column(name = "unit", nullable = false)
    private String unit;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    public SyntheticMetric() {
    }

    public SyntheticMetric(String metricId, String serviceName, String metricName, Double value, String unit, Instant timestamp) {
        this.metricId = metricId;
        this.serviceName = serviceName;
        this.metricName = metricName;
        this.value = value;
        this.unit = unit;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public String getMetricId() {
        return metricId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getMetricName() {
        return metricName;
    }

    public Double getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
