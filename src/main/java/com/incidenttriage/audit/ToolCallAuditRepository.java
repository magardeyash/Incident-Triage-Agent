package com.incidenttriage.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToolCallAuditRepository extends JpaRepository<ToolCallAudit, Long> {
    List<ToolCallAudit> findByInvestigationIdOrderByCreatedAtAsc(Long investigationId);
}
