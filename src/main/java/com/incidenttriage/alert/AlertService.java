package com.incidenttriage.alert;

import com.incidenttriage.alert.dto.AlertResponse;
import com.incidenttriage.alert.dto.CreateAlertRequest;
import com.incidenttriage.alert.dto.CreateAlertResponse;
import com.incidenttriage.common.exception.ResourceNotFoundException;
import com.incidenttriage.investigation.Investigation;
import com.incidenttriage.investigation.InvestigationExecutionService;
import com.incidenttriage.investigation.InvestigationService;
import com.incidenttriage.investigation.InvestigationStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AlertService {

    private final AlertRepository alertRepository;
    private final InvestigationService investigationService;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    public AlertService(AlertRepository alertRepository,
                        InvestigationService investigationService,
                        org.springframework.context.ApplicationEventPublisher eventPublisher) {
        this.alertRepository = alertRepository;
        this.investigationService = investigationService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public CreateAlertResponse processAlert(CreateAlertRequest request) {
        Instant occurredAt = request.timestamp() != null ? request.timestamp() : Instant.now();
        Alert alert = new Alert(
                request.service(),
                request.severity(),
                request.message(),
                occurredAt,
                request.payload()
        );
        Alert savedAlert = alertRepository.save(alert);
        Investigation investigation = investigationService.createInvestigation(savedAlert.getId());

        eventPublisher.publishEvent(new AlertCreatedEvent(savedAlert.getId(), investigation.getId()));

        return new CreateAlertResponse(savedAlert.getId(), investigation.getId(), investigation.getStatus());
    }

    @Transactional(readOnly = true)
    public AlertResponse getAlertById(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with ID: " + id));
        return AlertResponse.from(alert);
    }

    @Transactional(readOnly = true)
    public Alert getEntityById(Long id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with ID: " + id));
    }
}
