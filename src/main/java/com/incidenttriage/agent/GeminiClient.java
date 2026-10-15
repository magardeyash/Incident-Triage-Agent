package com.incidenttriage.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models";

    private final String apiKey;
    private final String model;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GeminiClient(
            @Value("${app.gemini.api-key:${GEMINI_API_KEY:}}") String apiKey,
            @Value("${app.gemini.model:gemini-2.5-flash}") String model,
            @Value("${app.gemini.timeout-seconds:15}") int timeoutSeconds,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.model = model;
        this.objectMapper = objectMapper;

        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofSeconds(timeoutSeconds))
                .withReadTimeout(Duration.ofSeconds(timeoutSeconds));
        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactories.get(settings);

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    public boolean isConfigured() {
        return !apiKey.isBlank();
    }

    public String getModelName() {
        return model;
    }

    public record GeminiResponse(
            String textContent,
            List<ToolCallRequest> toolCalls,
            int promptTokens,
            int candidateTokens
    ) {}

    public record ToolCallRequest(String name, Map<String, Object> arguments) {}

    public GeminiResponse generate(String systemPrompt, List<Map<String, Object>> contents, boolean withTools) {
        if (!isConfigured()) {
            throw new IllegalStateException("Gemini API key is not configured. Set GEMINI_API_KEY environment variable.");
        }

        Map<String, Object> requestBody = new HashMap<>();

        // System instruction
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            requestBody.put("systemInstruction", Map.of(
                    "parts", List.of(Map.of("text", systemPrompt))
            ));
        }

        requestBody.put("contents", contents);

        // Generation config
        requestBody.put("generationConfig", Map.of(
                "temperature", 0.1,
                "maxOutputTokens", 2048
        ));

        // Tool declarations
        if (withTools) {
            requestBody.put("tools", List.of(Map.of(
                    "functionDeclarations", getFunctionDeclarations()
            )));
        }

        String url = BASE_URL + "/" + model + ":generateContent?key=" + apiKey;

        try {
            String rawResponse = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            return parseGeminiResponse(rawResponse);
        } catch (Exception e) {
            log.error("Gemini model request failed: {}", e.getMessage());
            throw new RuntimeException("Gemini API request failed: " + e.getMessage(), e);
        }
    }

    private GeminiResponse parseGeminiResponse(String rawJson) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(rawJson);
        JsonNode candidates = root.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) {
            return new GeminiResponse("", List.of(), 0, 0);
        }

        JsonNode firstCandidate = candidates.get(0);
        JsonNode parts = firstCandidate.path("content").path("parts");

        StringBuilder textBuilder = new StringBuilder();
        List<ToolCallRequest> toolCalls = new ArrayList<>();

        if (parts.isArray()) {
            for (JsonNode part : parts) {
                if (part.has("text")) {
                    textBuilder.append(part.path("text").asText());
                }
                if (part.has("functionCall")) {
                    JsonNode fc = part.path("functionCall");
                    String funcName = fc.path("name").asText();
                    Map<String, Object> args = objectMapper.convertValue(fc.path("args"), Map.class);
                    toolCalls.add(new ToolCallRequest(funcName, args != null ? args : Map.of()));
                }
            }
        }

        int promptTokens = root.path("usageMetadata").path("promptTokenCount").asInt(0);
        int candidateTokens = root.path("usageMetadata").path("candidatesTokenCount").asInt(0);

        return new GeminiResponse(textBuilder.toString().trim(), toolCalls, promptTokens, candidateTokens);
    }

    private List<Map<String, Object>> getFunctionDeclarations() {
        return List.of(
                Map.of(
                        "name", "queryLogs",
                        "description", "Query application logs for a given service within an ISO timestamp window",
                        "parameters", Map.of(
                                "type", "OBJECT",
                                "properties", Map.of(
                                        "service", Map.of("type", "STRING", "description", "Service name, e.g. checkout-service"),
                                        "startTime", Map.of("type", "STRING", "description", "Start ISO timestamp, e.g. 2026-10-15T10:00:00Z"),
                                        "endTime", Map.of("type", "STRING", "description", "End ISO timestamp, e.g. 2026-10-15T10:30:00Z")
                                ),
                                "required", List.of("service", "startTime", "endTime")
                        )
                ),
                Map.of(
                        "name", "getMetrics",
                        "description", "Get operational metrics (error rate, latency) for a service within an ISO window",
                        "parameters", Map.of(
                                "type", "OBJECT",
                                "properties", Map.of(
                                        "service", Map.of("type", "STRING", "description", "Service name"),
                                        "startTime", Map.of("type", "STRING", "description", "Start ISO timestamp"),
                                        "endTime", Map.of("type", "STRING", "description", "End ISO timestamp")
                                ),
                                "required", List.of("service", "startTime", "endTime")
                        )
                ),
                Map.of(
                        "name", "getRecentDeploys",
                        "description", "Get recent deployments and software releases for a service",
                        "parameters", Map.of(
                                "type", "OBJECT",
                                "properties", Map.of(
                                        "service", Map.of("type", "STRING", "description", "Service name"),
                                        "startTime", Map.of("type", "STRING", "description", "Start ISO timestamp"),
                                        "endTime", Map.of("type", "STRING", "description", "End ISO timestamp")
                                ),
                                "required", List.of("service", "startTime", "endTime")
                        )
                ),
                Map.of(
                        "name", "getServiceDependencies",
                        "description", "Get upstream and downstream service dependencies",
                        "parameters", Map.of(
                                "type", "OBJECT",
                                "properties", Map.of(
                                        "service", Map.of("type", "STRING", "description", "Service name")
                                ),
                                "required", List.of("service")
                        )
                ),
                Map.of(
                        "name", "searchRunbooks",
                        "description", "Semantically search operational troubleshooting runbooks using keywords or symptom description",
                        "parameters", Map.of(
                                "type", "OBJECT",
                                "properties", Map.of(
                                        "query", Map.of("type", "STRING", "description", "Search query or symptoms")
                                ),
                                "required", List.of("query")
                        )
                )
        );
    }
}
