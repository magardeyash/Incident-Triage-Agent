package com.incidenttriage.operationaldata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SyntheticDependencyRepository extends JpaRepository<SyntheticServiceDependency, Long> {
    List<SyntheticServiceDependency> findBySourceService(String sourceService);
    List<SyntheticServiceDependency> findByTargetService(String targetService);
}
