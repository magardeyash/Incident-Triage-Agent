package com.incidenttriage.runbook;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RunbookChunker {

    private static final int DEFAULT_CHUNK_SIZE = 500;
    private static final int DEFAULT_OVERLAP = 50;

    public List<RunbookChunk> chunkText(String documentId, String title, String text) {
        return chunkText(documentId, title, text, DEFAULT_CHUNK_SIZE, DEFAULT_OVERLAP);
    }

    public List<RunbookChunk> chunkText(String documentId, String title, String text, int chunkSize, int overlap) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        List<RunbookChunk> chunks = new ArrayList<>();
        String normalized = text.trim();

        // Split primarily by markdown headers or double newlines (paragraphs)
        String[] paragraphs = normalized.split("\n\n+");
        StringBuilder currentChunk = new StringBuilder();
        int chunkIndex = 0;

        for (String paragraph : paragraphs) {
            String cleanPara = paragraph.trim();
            if (cleanPara.isEmpty()) continue;

            if (currentChunk.length() + cleanPara.length() + 2 > chunkSize && currentChunk.length() > 0) {
                chunks.add(new RunbookChunk(
                        documentId + "-chunk-" + chunkIndex,
                        documentId,
                        title,
                        currentChunk.toString().trim(),
                        chunkIndex
                ));
                chunkIndex++;

                // Overlap: keep tail of previous chunk if overlap requested
                if (overlap > 0 && currentChunk.length() > overlap) {
                    String tail = currentChunk.substring(currentChunk.length() - overlap);
                    currentChunk = new StringBuilder(tail).append("\n\n");
                } else {
                    currentChunk = new StringBuilder();
                }
            }

            if (currentChunk.length() > 0) {
                currentChunk.append("\n\n");
            }
            currentChunk.append(cleanPara);
        }

        if (currentChunk.length() > 0) {
            chunks.add(new RunbookChunk(
                    documentId + "-chunk-" + chunkIndex,
                    documentId,
                    title,
                    currentChunk.toString().trim(),
                    chunkIndex
            ));
        }

        return chunks;
    }
}
