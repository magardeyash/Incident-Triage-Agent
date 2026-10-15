package com.incidenttriage.runbook;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "runbook_documents")
public class RunbookDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", unique = true, nullable = false)
    private String documentId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "chunk_count", nullable = false)
    private int chunkCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public RunbookDocument() {
    }

    public RunbookDocument(String documentId, String title, String filename, String status, int chunkCount) {
        this.documentId = documentId;
        this.title = title;
        this.filename = filename;
        this.status = status;
        this.chunkCount = chunkCount;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getTitle() {
        return title;
    }

    public String getFilename() {
        return filename;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getChunkCount() {
        return chunkCount;
    }

    public void setChunkCount(int chunkCount) {
        this.chunkCount = chunkCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
