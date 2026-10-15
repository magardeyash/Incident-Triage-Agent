package com.incidenttriage.investigation;

import com.incidenttriage.agent.AgentOrchestrator;
import com.incidenttriage.alert.Alert;
import com.incidenttriage.alert.AlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class InvestigationExecutionService {

    private static final Logger log = LoggerFactory.getLogger(InvestigationExecutionService.class);

    private final AlertRepository alertRepository;
    private final InvestigationRepository investigationRepository;
    private final AgentOrchestrator agentOrchestrator;
    private final InvestigationService investigationService;

    public InvestigationExecutionService(AlertRepository alertRepository,
                                         InvestigationRepository investigationRepository,
                                         AgentOrchestrator agentOrchestrator,
                                         InvestigationService investigationService) {
        this.alertRepository = alertRepository;
        this.investigationRepository = investigationRepository;
        this.agentOrchestrator = agentOrchestrator;
        this.investigationService = investigationService;
    }

    @Async("investigationTaskExecutor")
    @org.springframework.transaction.event.TransactionalEventListener(
            phase = org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT,
            fallbackExecution = true
    )
    public void onAlertCreated(com.incidenttriage.alert.AlertCreatedEvent event) {
        executeInvestigation(event.alertId(), event.investigationId());
    }

    public void executeInvestigation(Long alertId, Long investigationId) {
        log.info("Worker thread starting investigation {} for alert {}", investigationId, alertId);
        try {
            Optional<Alert> alertOpt = alertRepository.findById(alertId);
            Optional<Investigation> invOpt = investigationRepository.findById(investigationId);

            if (alertOpt.isEmpty() || invOpt.isEmpty()) {
                log.error("Could not find alert {} or investigation {} in worker thread", alertId, investigationId);
                investigationService.updateStatus(investigationId, InvestigationStatus.FAILED, "Entity not found during async execution");
                return;
            }

            agentOrchestrator.orchestrateInvestigation(invOpt.get(), alertOpt.get());
        } catch (Exception e) {
            log.error("Async execution failed for investigation {}: {}", investigationId, e.getMessage(), e);
            investigationService.updateStatus(investigationId, InvestigationStatus.FAILED, e.getMessage());
        }
    }
}
