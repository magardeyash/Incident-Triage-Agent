package com.incidenttriage.report.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record IncidentReportDto(
        @NotBlank(message = "Summary must not be blank")
        @JsonPropertyDescription("A concise executive summary of the incident")
        String summary,

        @NotNull(message = "Probable root cause category must be specified")
        @JsonPropertyDescription("The primary root cause category: BAD_DEPLOY, DEPENDENCY_FAILURE, RESOURCE_EXHAUSTION, CONFIGURATION_ERROR, or UNKNOWN")
        RootCauseCategory probableRootCause,

        @NotNull(message = "Confidence score must be provided")
        @DecimalMin(value = "0.0", message = "Confidence must be between 0.0 and 1.0")
        @DecimalMax(value = "1.0", message = "Confidence must be between 0.0 and 1.0")
        @JsonPropertyDescription("Confidence in the diagnosis from 0.0 to 1.0")
        Double confidence,

        @NotEmpty(message = "At least one evidence item must be cited")
        @Size(max = 20, message = "Cannot cite more than 20 evidence items")
        @JsonPropertyDescription("List of exact evidence IDs cited to substantiate the conclusion, e.g. EVID-LOG-chk-003")
        List<String> evidence,

        @NotEmpty(message = "At least one recommendation must be provided")
        @Size(max = 10, message = "Cannot provide more than 10 recommendations")
        @JsonPropertyDescription("Concrete recommended next steps for human operators")
        List<String> recommendations,

        @JsonPropertyDescription("Known missing evidence, observational gaps, or alternative hypotheses")
        String limitations
) {
}
