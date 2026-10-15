package com.incidenttriage.alert;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.incidenttriage.alert.dto.CreateAlertRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AlertControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String VALID_API_KEY = "test-api-key";

    @Test
    @DisplayName("Should reject requests missing API key with 401")
    void shouldRejectMissingApiKey() throws Exception {
        CreateAlertRequest request = new CreateAlertRequest(
                "checkout-service",
                "HIGH",
                "High error rate detected",
                Instant.now(),
                null
        );

        mockMvc.perform(post("/api/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Should reject requests with invalid API key with 401")
    void shouldRejectInvalidApiKey() throws Exception {
        CreateAlertRequest request = new CreateAlertRequest(
                "checkout-service",
                "HIGH",
                "High error rate detected",
                Instant.now(),
                null
        );

        mockMvc.perform(post("/api/alerts")
                        .header(API_KEY_HEADER, "invalid-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Should reject invalid request body with 400")
    void shouldRejectInvalidRequestBody() throws Exception {
        CreateAlertRequest invalidRequest = new CreateAlertRequest(
                "", // Blank service
                "HIGH",
                "High error rate detected",
                Instant.now(),
                null
        );

        mockMvc.perform(post("/api/alerts")
                        .header(API_KEY_HEADER, VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("Should accept valid alert, create investigation, and retrieve them")
    void shouldAcceptAlertAndCreateInvestigation() throws Exception {
        CreateAlertRequest request = new CreateAlertRequest(
                "checkout-service",
                "CRITICAL",
                "Payment gateway timeout rate > 20%",
                Instant.now(),
                "{\"threshold\": 0.20}"
        );

        String postResponse = mockMvc.perform(post("/api/alerts")
                        .header(API_KEY_HEADER, VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.alertId", notNullValue()))
                .andExpect(jsonPath("$.investigationId", notNullValue()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();

        Long alertId = objectMapper.readTree(postResponse).get("alertId").asLong();
        Long investigationId = objectMapper.readTree(postResponse).get("investigationId").asLong();

        // Retrieve alert
        mockMvc.perform(get("/api/alerts/" + alertId)
                        .header(API_KEY_HEADER, VALID_API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(alertId))
                .andExpect(jsonPath("$.serviceName").value("checkout-service"))
                .andExpect(jsonPath("$.severity").value("CRITICAL"))
                .andExpect(jsonPath("$.message").value("Payment gateway timeout rate > 20%"));

        // Retrieve investigation
        mockMvc.perform(get("/api/investigations/" + investigationId)
                        .header(API_KEY_HEADER, VALID_API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(investigationId))
                .andExpect(jsonPath("$.alertId").value(alertId))
                .andExpect(jsonPath("$.status", org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.equalTo("PENDING"),
                        org.hamcrest.Matchers.equalTo("RUNNING"),
                        org.hamcrest.Matchers.equalTo("COMPLETED")
                )));
    }

    @Test
    @DisplayName("Should return 404 for nonexistent alert")
    void shouldReturn404ForNonexistentAlert() throws Exception {
        mockMvc.perform(get("/api/alerts/999999")
                        .header(API_KEY_HEADER, VALID_API_KEY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message", containsString("999999")));
    }

    @Test
    @DisplayName("Should return 404 for nonexistent investigation")
    void shouldReturn404ForNonexistentInvestigation() throws Exception {
        mockMvc.perform(get("/api/investigations/999999")
                        .header(API_KEY_HEADER, VALID_API_KEY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message", containsString("999999")));
    }
}
