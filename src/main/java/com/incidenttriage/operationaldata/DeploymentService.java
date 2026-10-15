package com.incidenttriage.operationaldata;

import com.incidenttriage.evidence.EvidenceItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class DeploymentService {

    private static final Duration MAX_WINDOW = Duration.ofDays(7);
    private static final int MAX_RESULTS = 50;

    private final SyntheticDeploymentRepository deploymentRepository;

    public DeploymentService(SyntheticDeploymentRepository deploymentRepository) {
        this.deploymentRepository = deploymentRepository;
    }

    @Transactional(readOnly = true)
    public List<SyntheticDeployment> getRecentDeploys(String serviceName, Instant startTime, Instant endTime) {
        validateWindow(startTime, endTime);
        List<SyntheticDeployment> deploys = deploymentRepository.findByServiceNameAndDeployedAtBetweenOrderByDeployedAtDesc(
                serviceName, startTime, endTime);
        if (deploys.size() > MAX_RESULTS) {
            deploys = deploys.subList(0, MAX_RESULTS);
        }
        return deploys;
    }

    public List<EvidenceItem> getRecentDeploysAsEvidence(String serviceName, Instant startTime, Instant endTime) {
        return getRecentDeploys(serviceName, startTime, endTime).stream()
                .map(d -> EvidenceItem.fromDeployment(
                        d.getDeploymentId(),
                        d.getServiceName(),
                        d.getDeployedAt(),
                        d.getVersion(),
                        d.getStatus(),
                        d.getSummary()
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
            throw new IllegalArgumentException("Time window cannot exceed " + MAX_WINDOW.toDays() + " days");
        }
    }
}
