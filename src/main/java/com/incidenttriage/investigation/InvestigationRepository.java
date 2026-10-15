package com.incidenttriage.investigation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvestigationRepository extends JpaRepository<Investigation, Long> {
    Optional<Investigation> findByAlertId(Long alertId);
    List<Investigation> findByStatus(InvestigationStatus status);
    List<Investigation> findAllByOrderByCreatedAtDesc();
}
