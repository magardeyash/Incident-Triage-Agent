package com.incidenttriage.runbook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RunbookDocumentRepository extends JpaRepository<RunbookDocument, Long> {
    Optional<RunbookDocument> findByDocumentId(String documentId);
}
