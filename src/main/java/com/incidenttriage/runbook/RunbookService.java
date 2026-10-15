package com.incidenttriage.runbook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class RunbookService {

    private static final Logger log = LoggerFactory.getLogger(RunbookService.class);
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(".md", ".txt", ".markdown");

    private final RunbookDocumentRepository documentRepository;
    private final RunbookChunker chunker;
    private final EmbeddingService embeddingService;
    private final RunbookSearchService searchService;

    public record IngestRunbookRequest(
            String title,
            String filename,
            String content
    ) {}

    public record IngestRunbookResponse(
            String documentId,
            String title,
            String status,
            int chunkCount
    ) {}

    public RunbookService(RunbookDocumentRepository documentRepository,
                          RunbookChunker chunker,
                          EmbeddingService embeddingService,
                          RunbookSearchService searchService) {
        this.documentRepository = documentRepository;
        this.chunker = chunker;
        this.embeddingService = embeddingService;
        this.searchService = searchService;
    }

    @Transactional
    public IngestRunbookResponse ingestRunbook(IngestRunbookRequest request) {
        validateRequest(request);

        String documentId = "RB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        List<RunbookChunk> chunks = chunker.chunkText(documentId, request.title(), request.content());

        for (RunbookChunk chunk : chunks) {
            float[] vector = embeddingService.computeEmbedding(chunk.content());
            searchService.storeChunk(chunk, vector);
        }

        RunbookDocument doc = new RunbookDocument(
                documentId,
                request.title(),
                request.filename(),
                "INGESTED",
                chunks.size()
        );
        documentRepository.save(doc);

        log.info("Ingested runbook {} ('{}') into {} chunks", documentId, request.title(), chunks.size());
        return new IngestRunbookResponse(documentId, request.title(), "INGESTED", chunks.size());
    }

    private void validateRequest(IngestRunbookRequest req) {
        if (req.title() == null || req.title().isBlank()) {
            throw new IllegalArgumentException("Runbook title must not be blank");
        }
        if (req.filename() == null || req.filename().isBlank()) {
            throw new IllegalArgumentException("Filename must not be blank");
        }
        boolean supported = SUPPORTED_EXTENSIONS.stream().anyMatch(ext -> req.filename().toLowerCase().endsWith(ext));
        if (!supported) {
            throw new IllegalArgumentException("Unsupported file type for filename '" + req.filename() +
                    "'. Supported formats are: " + SUPPORTED_EXTENSIONS);
        }
        if (req.content() == null || req.content().isBlank()) {
            throw new IllegalArgumentException("Runbook content must not be empty");
        }
    }
}
