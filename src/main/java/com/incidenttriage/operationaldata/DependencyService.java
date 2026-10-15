package com.incidenttriage.operationaldata;

import com.incidenttriage.evidence.EvidenceItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DependencyService {

    private final SyntheticDependencyRepository dependencyRepository;

    public DependencyService(SyntheticDependencyRepository dependencyRepository) {
        this.dependencyRepository = dependencyRepository;
    }

    @Transactional(readOnly = true)
    public List<SyntheticServiceDependency> getServiceDependencies(String serviceName) {
        if (serviceName == null || serviceName.isBlank()) {
            throw new IllegalArgumentException("Service name must not be blank");
        }
        return dependencyRepository.findBySourceService(serviceName);
    }

    public List<EvidenceItem> getServiceDependenciesAsEvidence(String serviceName) {
        return getServiceDependencies(serviceName).stream()
                .map(d -> EvidenceItem.fromDependency(
                        d.getDependencyId(),
                        d.getSourceService(),
                        d.getTargetService(),
                        d.getDependencyType(),
                        d.isCritical()
                ))
                .toList();
    }
}
