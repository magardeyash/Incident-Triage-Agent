package com.incidenttriage.runbook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.incidenttriage.evidence.EvidenceItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RunbookSearchService {

    private static final Logger log = LoggerFactory.getLogger(RunbookSearchService.class);

    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingService embeddingService;
    private final ObjectMapper objectMapper;

    // In-memory fallback index for H2 and offline test runs
    private final Map<String, InMemoryChunk> inMemoryStore = new ConcurrentHashMap<>();

    private record InMemoryChunk(RunbookChunk chunk, float[] embedding) {}

    public record SearchResult(
            String chunkId,
            String documentId,
            String title,
            String content,
            double similarityScore
    ) {}

    public RunbookSearchService(JdbcTemplate jdbcTemplate,
                                EmbeddingService embeddingService,
                                ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.embeddingService = embeddingService;
        this.objectMapper = objectMapper;
    }

    public void storeChunk(RunbookChunk chunk, float[] embedding) {
        inMemoryStore.put(chunk.chunkId(), new InMemoryChunk(chunk, embedding));

        try {
            String metadataJson = objectMapper.writeValueAsString(Map.of(
                    "chunkId", chunk.chunkId(),
                    "documentId", chunk.documentId(),
                    "title", chunk.title()
            ));

            String vectorString = formatVectorForPostgres(embedding);

            jdbcTemplate.update(
                    "INSERT INTO vector_store (content, metadata, embedding) VALUES (?, ?::jsonb, ?::vector)",
                    chunk.content(), metadataJson, vectorString
            );
        } catch (Exception e) {
            log.debug("Database vector store insert bypassed (likely H2 in test mode): {}", e.getMessage());
        }
    }

    public List<SearchResult> search(String query, int topK, double minSimilarity) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        float[] queryVector = embeddingService.computeEmbedding(query);

        // Try pgvector database query first
        try {
            String vectorString = formatVectorForPostgres(queryVector);
            String sql = "SELECT content, metadata, 1 - (embedding <=> ?::vector) AS similarity " +
                    "FROM vector_store " +
                    "WHERE 1 - (embedding <=> ?::vector) >= ? " +
                    "ORDER BY similarity DESC LIMIT ?";

            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                String content = rs.getString("content");
                String metadataJson = rs.getString("metadata");
                double similarity = rs.getDouble("similarity");

                String chunkId = "";
                String docId = "";
                String title = "";
                try {
                    Map<String, Object> meta = objectMapper.readValue(metadataJson, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                    chunkId = String.valueOf(meta.getOrDefault("chunkId", ""));
                    docId = String.valueOf(meta.getOrDefault("documentId", ""));
                    title = String.valueOf(meta.getOrDefault("title", ""));
                } catch (Exception ignored) {}

                return new SearchResult(chunkId, docId, title, content, similarity);
            }, vectorString, vectorString, minSimilarity, topK);

        } catch (Exception e) {
            log.debug("Falling back to in-memory cosine similarity search: {}", e.getMessage());
            return searchInMemory(queryVector, topK, minSimilarity);
        }
    }

    public List<EvidenceItem> searchAsEvidence(String query, int topK, double minSimilarity) {
        return search(query, topK, minSimilarity).stream()
                .map(res -> EvidenceItem.fromRunbookChunk(
                        res.chunkId(),
                        res.title(),
                        "Runbook [" + res.title() + "] says: " + res.content()
                ))
                .toList();
    }

    private List<SearchResult> searchInMemory(float[] queryVector, int topK, double minSimilarity) {
        List<SearchResult> results = new ArrayList<>();

        for (InMemoryChunk entry : inMemoryStore.values()) {
            double similarity = cosineSimilarity(queryVector, entry.embedding());
            if (similarity >= minSimilarity) {
                results.add(new SearchResult(
                        entry.chunk().chunkId(),
                        entry.chunk().documentId(),
                        entry.chunk().title(),
                        entry.chunk().content(),
                        similarity
                ));
            }
        }

        results.sort(Comparator.comparingDouble(SearchResult::similarityScore).reversed());
        if (results.size() > topK) {
            return results.subList(0, topK);
        }
        return results;
    }

    private double cosineSimilarity(float[] a, float[] b) {
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) return 0.0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private String formatVectorForPostgres(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(vector[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
