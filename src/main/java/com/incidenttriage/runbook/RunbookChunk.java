package com.incidenttriage.runbook;

public record RunbookChunk(
        String chunkId,
        String documentId,
        String title,
        String content,
        int chunkIndex
) {
}
