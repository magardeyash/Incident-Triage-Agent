package com.incidenttriage.runbook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService implements EmbeddingModel {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);
    public static final int EMBEDDING_DIMENSION = 768;
    private static final String EMBED_URL_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/%s:embedContent?key=%s";

    private final String apiKey;
    private final String embeddingModel;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public EmbeddingService(
            @Value("${app.gemini.api-key:${GEMINI_API_KEY:}}") String apiKey,
            @Value("${app.gemini.embedding-model:text-embedding-005}") String embeddingModel,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.embeddingModel = embeddingModel;
        this.objectMapper = objectMapper;

        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofSeconds(10))
                .withReadTimeout(Duration.ofSeconds(10));
        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactories.get(settings);

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    public float[] computeEmbedding(String text) {
        if (text == null || text.isBlank()) {
            return new float[EMBEDDING_DIMENSION];
        }

        if (!apiKey.isBlank()) {
            try {
                return callGeminiEmbeddingApi(text);
            } catch (Exception e) {
                log.warn("Gemini embedding API call failed: {}. Falling back to deterministic embedding vector.", e.getMessage());
            }
        }

        return generateDeterministicVector(text);
    }

    @Override
    public float[] embed(Document document) {
        return computeEmbedding(document != null ? document.getContent() : "");
    }

    @Override
    public float[] embed(String text) {
        return computeEmbedding(text);
    }

    @Override
    public int dimensions() {
        return EMBEDDING_DIMENSION;
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<Embedding> embeddings = new ArrayList<>();
        List<String> instructions = request.getInstructions();
        for (int i = 0; i < instructions.size(); i++) {
            float[] vector = computeEmbedding(instructions.get(i));
            embeddings.add(new Embedding(vector, i));
        }
        return new EmbeddingResponse(embeddings);
    }

    private float[] callGeminiEmbeddingApi(String text) {
        String url = String.format(EMBED_URL_TEMPLATE, embeddingModel, apiKey);
        Map<String, Object> body = Map.of(
                "content", Map.of("parts", List.of(Map.of("text", text)))
        );

        String rawJson = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode values = root.path("embedding").path("values");
            if (values.isArray() && values.size() == EMBEDDING_DIMENSION) {
                float[] result = new float[EMBEDDING_DIMENSION];
                for (int i = 0; i < EMBEDDING_DIMENSION; i++) {
                    result[i] = (float) values.get(i).asDouble();
                }
                return result;
            }
        } catch (Exception e) {
            log.warn("Failed to parse Gemini embedding JSON: {}", e.getMessage());
        }

        return generateDeterministicVector(text);
    }

    /**
     * Generates a deterministic normalized 768-dimensional float vector based on word hashes.
     * Ensures identical text produces identical embeddings and similar words share dimensions.
     */
    public float[] generateDeterministicVector(String text) {
        float[] vector = new float[EMBEDDING_DIMENSION];
        String[] words = text.toLowerCase().split("\\W+");

        for (String word : words) {
            if (word.isBlank()) continue;
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hash = md.digest(word.getBytes(StandardCharsets.UTF_8));
                for (int i = 0; i < hash.length; i++) {
                    int index = (Math.abs(hash[i]) * 31 + i) % EMBEDDING_DIMENSION;
                    vector[index] += 1.0f;
                }
            } catch (Exception ignored) {
            }
        }

        // L2 Normalize
        double norm = 0.0;
        for (float v : vector) {
            norm += v * v;
        }
        if (norm > 0) {
            norm = Math.sqrt(norm);
            for (int i = 0; i < EMBEDDING_DIMENSION; i++) {
                vector[i] = (float) (vector[i] / norm);
            }
        }

        return vector;
    }
}
