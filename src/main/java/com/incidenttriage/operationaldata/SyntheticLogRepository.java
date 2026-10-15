package com.incidenttriage.operationaldata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SyntheticLogRepository extends JpaRepository<SyntheticLog, Long> {
    List<SyntheticLog> findByServiceNameAndTimestampBetweenOrderByTimestampAsc(
            String serviceName, Instant startTime, Instant endTime);

    List<SyntheticLog> findByServiceNameOrderByTimestampAsc(String serviceName);
}
