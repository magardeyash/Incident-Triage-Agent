package com.incidenttriage.alert;

import com.incidenttriage.alert.dto.AlertResponse;
import com.incidenttriage.alert.dto.CreateAlertRequest;
import com.incidenttriage.alert.dto.CreateAlertResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alerts")
@Tag(name = "Alerts", description = "Endpoints for alert ingestion and inspection")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @PostMapping
    @Operation(summary = "Submit a new alert and initiate investigation", description = "Accepts an alert and creates an asynchronous investigation record")
    @ApiResponse(responseCode = "202", description = "Alert accepted for investigation")
    public ResponseEntity<CreateAlertResponse> submitAlert(@Valid @RequestBody CreateAlertRequest request) {
        CreateAlertResponse response = alertService.processAlert(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve alert by ID")
    @ApiResponse(responseCode = "200", description = "Alert found")
    @ApiResponse(responseCode = "404", description = "Alert not found")
    public ResponseEntity<AlertResponse> getAlert(@PathVariable Long id) {
        AlertResponse response = alertService.getAlertById(id);
        return ResponseEntity.ok(response);
    }
}
