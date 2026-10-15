package com.incidenttriage.runbook;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class RunbookServiceTest {

    @Autowired
    private RunbookService runbookService;

    @Autowired
    private RunbookSearchService searchService;

    @Test
    @DisplayName("Should ingest valid markdown runbook and split into chunks")
    void testIngestRunbook() {
        String content = """
                # Payment Gateway Outage Runbook
                
                ## Symptoms
                When payment-service response time exceeds 5000ms or error rate > 5%,
                investigate downstream gateway connectivity.
                
                ## Mitigation Steps
                1. Check external provider status page.
                2. Increase circuit breaker timeout temporarily.
                3. Switch traffic to backup payment processor if primary remains down.
                """;

        RunbookService.IngestRunbookRequest request = new RunbookService.IngestRunbookRequest(
                "Payment Gateway Runbook",
                "payment-runbook.md",
                content
        );

        RunbookService.IngestRunbookResponse response = runbookService.ingestRunbook(request);
        assertThat(response.documentId()).startsWith("RB-");
        assertThat(response.status()).isEqualTo("INGESTED");
        assertThat(response.chunkCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should reject unsupported file extensions and blank content")
    void testRejectInvalidRunbook() {
        assertThatThrownBy(() -> runbookService.ingestRunbook(new RunbookService.IngestRunbookRequest(
                "Title", "malicious.exe", "content")))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> runbookService.ingestRunbook(new RunbookService.IngestRunbookRequest(
                "Title", "runbook.md", "   ")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should semantically search ingested runbook chunks")
    void testSemanticRetrieval() {
        String content = """
                # Database Connection Pool Troubleshooting
                
                When HikariCP connection pool gets exhausted:
                Run `SELECT * FROM pg_stat_activity` to find long-running lock queries.
                Kill queries blocking transactions.
                """;

        runbookService.ingestRunbook(new RunbookService.IngestRunbookRequest(
                "Database Pool Runbook",
                "db-pool.md",
                content
        ));

        List<RunbookSearchService.SearchResult> results = searchService.search("HikariCP connection pool exhausted", 3, 0.1);
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).content()).contains("HikariCP");
    }
}
