package com.incidenttriage.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final ToolCallAuditRepository auditRepository;

    public AuditService(ToolCallAuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordToolCall(Long investigationId, String toolName, String sanitizedArgs,
                               String resultSummary, long durationMs, boolean success, String errorMessage) {
        try {
            ToolCallAudit audit = new ToolCallAudit(
                    investigationId,
                    toolName,
                    truncate(sanitizedArgs, 1000),
                    truncate(resultSummary, 1000),
                    durationMs,
                    success,
                    truncate(errorMessage, 500)
            );
            auditRepository.save(audit);
        } catch (Exception e) {
            log.warn("Failed to persist tool call audit record: {}", e.getMessage());
        }
    }

    private String truncate(String text, int max) {
        if (text == null) return null;
        return text.length() <= max ? text : text.substring(0, max) + "... [truncated]";
    }
}
