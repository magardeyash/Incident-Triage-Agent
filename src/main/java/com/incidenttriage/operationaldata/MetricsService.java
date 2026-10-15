package com.incidenttriage.operationaldata;

import com.incidenttriage.evidence.EvidenceItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class MetricsService {

    private static final Duration MAX_WINDOW = Duration.ofHours(24);
    private static final int MAX_RESULTS = 100;

    private final SyntheticMetricRepository metricRepository;

    public MetricsService(SyntheticMetricRepository metricRepository) {
        this.metricRepository = metricRepository;
    }

    @Transactional(readOnly = true)
    public List<SyntheticMetric> getMetrics(String serviceName, Instant startTime, Instant endTime) {
        validateWindow(startTime, endTime);
        List<SyntheticMetric> metrics = metricRepository.findByServiceNameAndTimestampBetweenOrderByTimestampAsc(
                serviceName, startTime, endTime);
        if (metrics.size() > MAX_RESULTS) {
            metrics = metrics.subList(0, MAX_RESULTS);
        }
        return metrics;
    }

    public List<EvidenceItem> getMetricsAsEvidence(String serviceName, Instant startTime, Instant endTime) {
        return getMetrics(serviceName, startTime, endTime).stream()
                .map(m -> EvidenceItem.fromMetric(
                        m.getMetricId(),
                        m.getServiceName(),
                        m.getTimestamp(),
                        m.getMetricName(),
                        m.getValue(),
                        m.getUnit()
                ))
                .toList();
    }

    private void validateWindow(Instant startTime, Instant endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start time and end time must not be null");
        }
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start time must not be after end time");
        }
        if (Duration.between(startTime, endTime).compareTo(MAX_WINDOW) > 0) {
            throw new IllegalArgumentException("Time window cannot exceed " + MAX_WINDOW.toHours() + " hours");
        }
    }
}
