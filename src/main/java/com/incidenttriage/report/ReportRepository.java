package com.incidenttriage.report;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<InvestigationReport, Long> {
    Optional<InvestigationReport> findByInvestigationId(Long investigationId);
}
