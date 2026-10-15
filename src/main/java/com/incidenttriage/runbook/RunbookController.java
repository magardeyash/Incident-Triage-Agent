package com.incidenttriage.runbook;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/runbooks")
@Tag(name = "Runbooks", description = "Endpoints for runbook ingestion and semantic vector retrieval")
public class RunbookController {

    private final RunbookService runbookService;
    private final RunbookSearchService searchService;

    public RunbookController(RunbookService runbookService, RunbookSearchService searchService) {
        this.runbookService = runbookService;
        this.searchService = searchService;
    }

    @PostMapping
    @Operation(summary = "Ingest a new runbook", description = "Extracts, chunks, embeds and persists runbook text in pgvector")
    @ApiResponse(responseCode = "201", description = "Runbook ingested")
    public ResponseEntity<RunbookService.IngestRunbookResponse> ingestRunbook(@RequestBody RunbookService.IngestRunbookRequest request) {
        RunbookService.IngestRunbookResponse response = runbookService.ingestRunbook(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Semantic search for runbook troubleshooting guidance")
    @ApiResponse(responseCode = "200", description = "Matching runbook chunks")
    public ResponseEntity<List<RunbookSearchService.SearchResult>> search(
            @RequestParam("q") String query,
            @RequestParam(value = "topK", defaultValue = "3") int topK,
            @RequestParam(value = "minSimilarity", defaultValue = "0.3") double minSimilarity) {
        List<RunbookSearchService.SearchResult> results = searchService.search(query, topK, minSimilarity);
        return ResponseEntity.ok(results);
    }
}
