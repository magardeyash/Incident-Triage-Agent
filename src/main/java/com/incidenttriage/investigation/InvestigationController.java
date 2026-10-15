package com.incidenttriage.investigation;

import com.incidenttriage.investigation.dto.InvestigationResponse;
import com.incidenttriage.report.ReportService;
import com.incidenttriage.report.dto.ReportResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/investigations")
@Tag(name = "Investigations", description = "Endpoints for checking investigation status and retrieving reports")
public class InvestigationController {

    private final InvestigationService investigationService;
    private final ReportService reportService;

    public InvestigationController(InvestigationService investigationService, ReportService reportService) {
        this.investigationService = investigationService;
        this.reportService = reportService;
    }

    @GetMapping
    @Operation(summary = "List all recent investigations")
    @ApiResponse(responseCode = "200", description = "Investigations list")
    public ResponseEntity<java.util.List<InvestigationResponse>> listInvestigations() {
        return ResponseEntity.ok(investigationService.getAllInvestigations());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve investigation status and metadata")
    @ApiResponse(responseCode = "200", description = "Investigation found")
    @ApiResponse(responseCode = "404", description = "Investigation not found")
    public ResponseEntity<InvestigationResponse> getInvestigation(@PathVariable Long id) {
        InvestigationResponse response = investigationService.getInvestigationById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/report")
    @Operation(summary = "Retrieve investigation report")
    @ApiResponse(responseCode = "200", description = "Report found")
    @ApiResponse(responseCode = "404", description = "Report or investigation not found")
    public ResponseEntity<ReportResponse> getReport(@PathVariable Long id) {
        ReportResponse response = reportService.getReportByInvestigationId(id);
        return ResponseEntity.ok(response);
    }
}
