package com.incidenttriage.investigation;

import com.incidenttriage.common.exception.ResourceNotFoundException;
import com.incidenttriage.investigation.dto.InvestigationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class InvestigationService {

    private final InvestigationRepository investigationRepository;

    public InvestigationService(InvestigationRepository investigationRepository) {
        this.investigationRepository = investigationRepository;
    }

    @Transactional
    public Investigation createInvestigation(Long alertId) {
        Investigation investigation = new Investigation(alertId);
        return investigationRepository.save(investigation);
    }

    @Transactional(readOnly = true)
    public InvestigationResponse getInvestigationById(Long id) {
        Investigation investigation = investigationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Investigation not found with ID: " + id));
        return InvestigationResponse.from(investigation);
    }

    @Transactional(readOnly = true)
    public Investigation getEntityById(Long id) {
        return investigationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Investigation not found with ID: " + id));
    }

    @Transactional
    public Investigation updateStatus(Long id, InvestigationStatus status, String errorMessage) {
        Investigation investigation = getEntityById(id);
        investigation.setStatus(status);
        if (status == InvestigationStatus.RUNNING && investigation.getStartedAt() == null) {
            investigation.setStartedAt(Instant.now());
        } else if (status == InvestigationStatus.COMPLETED || status == InvestigationStatus.FAILED || status == InvestigationStatus.INCONCLUSIVE) {
            investigation.setFinishedAt(Instant.now());
        }
        if (errorMessage != null) {
            investigation.setErrorMessage(errorMessage);
        }
        return investigationRepository.save(investigation);
    }

    @Transactional(readOnly = true)
    public List<InvestigationResponse> getAllInvestigations() {
        return investigationRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(InvestigationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Investigation> getPendingOrRunningInvestigations() {
        List<Investigation> pending = investigationRepository.findByStatus(InvestigationStatus.PENDING);
        List<Investigation> running = investigationRepository.findByStatus(InvestigationStatus.RUNNING);
        pending.addAll(running);
        return pending;
    }
}
