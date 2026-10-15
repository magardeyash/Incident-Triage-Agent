package com.incidenttriage.investigation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class StartupRecoveryService {

    private static final Logger log = LoggerFactory.getLogger(StartupRecoveryService.class);

    private final InvestigationRepository investigationRepository;

    public StartupRecoveryService(InvestigationRepository investigationRepository) {
        this.investigationRepository = investigationRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void recoverStaleInvestigations() {
        List<Investigation> pending = investigationRepository.findByStatus(InvestigationStatus.PENDING);
        List<Investigation> running = investigationRepository.findByStatus(InvestigationStatus.RUNNING);

        int count = 0;
        for (Investigation inv : pending) {
            inv.setStatus(InvestigationStatus.FAILED);
            inv.setErrorMessage("Process restarted while investigation was pending");
            inv.setFinishedAt(Instant.now());
            investigationRepository.save(inv);
            count++;
        }

        for (Investigation inv : running) {
            inv.setStatus(InvestigationStatus.FAILED);
            inv.setErrorMessage("Process restarted while investigation was in progress");
            inv.setFinishedAt(Instant.now());
            investigationRepository.save(inv);
            count++;
        }

        if (count > 0) {
            log.info("Marked {} stale investigations as FAILED after application startup", count);
        }
    }
}
