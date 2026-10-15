package com.incidenttriage.operationaldata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SyntheticMetricRepository extends JpaRepository<SyntheticMetric, Long> {
    List<SyntheticMetric> findByServiceNameAndTimestampBetweenOrderByTimestampAsc(
            String serviceName, Instant startTime, Instant endTime);

    List<SyntheticMetric> findByServiceNameAndMetricNameAndTimestampBetweenOrderByTimestampAsc(
            String serviceName, String metricName, Instant startTime, Instant endTime);
}
