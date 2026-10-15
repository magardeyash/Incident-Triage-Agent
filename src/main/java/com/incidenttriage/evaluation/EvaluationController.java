package com.incidenttriage.evaluation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/evaluations")
@Tag(name = "Evaluations", description = "Endpoints for running automated agent evaluation benchmarks")
public class EvaluationController {

    private final EvaluationRunner evaluationRunner;

    public EvaluationController(EvaluationRunner evaluationRunner) {
        this.evaluationRunner = evaluationRunner;
    }

    @PostMapping("/run")
    @Operation(summary = "Run synthetic evaluation suite", description = "Executes the 8 deterministic benchmark scenarios and reports accuracy, citation validity, and defense metrics")
    @ApiResponse(responseCode = "200", description = "Evaluation suite completed")
    public ResponseEntity<EvaluationSummary> runEvaluations() {
        EvaluationSummary summary = evaluationRunner.runEvaluationSuite();
        return ResponseEntity.ok(summary);
    }
}
