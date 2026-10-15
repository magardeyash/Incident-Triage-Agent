package com.incidenttriage.alert.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record CreateAlertRequest(
        @NotBlank(message = "Service name must not be blank")
        @JsonAlias({"service", "service_name"})
        String service,

        @NotBlank(message = "Severity must not be blank")
        String severity,

        @NotBlank(message = "Message must not be blank")
        String message,

        Instant timestamp,

        String payload
) {
}
