package com.incidenttriage.operationaldata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SyntheticDeploymentRepository extends JpaRepository<SyntheticDeployment, Long> {
    List<SyntheticDeployment> findByServiceNameAndDeployedAtBetweenOrderByDeployedAtDesc(
            String serviceName, Instant startTime, Instant endTime);

    List<SyntheticDeployment> findByServiceNameOrderByDeployedAtDesc(String serviceName);
}
