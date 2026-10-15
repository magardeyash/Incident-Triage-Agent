package com.incidenttriage.operationaldata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class SyntheticDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SyntheticDataSeeder.class);

    private final SyntheticLogRepository logRepo;
    private final SyntheticMetricRepository metricRepo;
    private final SyntheticDeploymentRepository deployRepo;
    private final SyntheticDependencyRepository depRepo;

    public SyntheticDataSeeder(SyntheticLogRepository logRepo,
                               SyntheticMetricRepository metricRepo,
                               SyntheticDeploymentRepository deployRepo,
                               SyntheticDependencyRepository depRepo) {
        this.logRepo = logRepo;
        this.metricRepo = metricRepo;
        this.deployRepo = deployRepo;
        this.depRepo = depRepo;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (logRepo.count() > 0) {
            return;
        }

        log.info("Seeding synthetic operational data...");

        // 1. Dependencies
        depRepo.saveAll(List.of(
                new SyntheticServiceDependency("DEPGRAPH-chk-pay", "checkout-service", "payment-service", "SYNC_HTTP", true),
                new SyntheticServiceDependency("DEPGRAPH-chk-inv", "checkout-service", "inventory-service", "SYNC_HTTP", true),
                new SyntheticServiceDependency("DEPGRAPH-pay-gw", "payment-service", "external-stripe-api", "SYNC_HTTP", true),
                new SyntheticServiceDependency("DEPGRAPH-inv-db", "inventory-service", "inventory-postgres", "DATABASE", true)
        ));

        // 2. Deployments
        deployRepo.saveAll(List.of(
                new SyntheticDeployment("DEP-chk-v1.4.2", "checkout-service", "v1.4.2", "SUCCESS", Instant.parse("2026-10-15T10:15:00Z"), "c8f39a1", "Feature: cart discounts module"),
                new SyntheticDeployment("DEP-chk-v1.4.1", "checkout-service", "v1.4.1", "SUCCESS", Instant.parse("2026-10-15T08:00:00Z"), "b4e12c9", "Chore: bump logback version"),
                new SyntheticDeployment("DEP-pay-v2.1.0", "payment-service", "v2.1.0", "SUCCESS", Instant.parse("2026-10-15T09:30:00Z"), "a71b3e8", "Fix: retry logic on stripe client"),
                new SyntheticDeployment("DEP-inv-v1.1.0", "inventory-service", "v1.1.0", "SUCCESS", Instant.parse("2026-10-14T18:00:00Z"), "99a4bc1", "Initial stable inventory release")
        ));

        // 3. Logs
        logRepo.saveAll(List.of(
                new SyntheticLog("LOG-chk-001", "checkout-service", Instant.parse("2026-10-15T10:14:50Z"), "INFO", "checkout-service v1.4.1 handling checkout request for order-1092", "{\"orderId\":\"order-1092\"}"),
                new SyntheticLog("LOG-chk-002", "checkout-service", Instant.parse("2026-10-15T10:16:05Z"), "INFO", "checkout-service v1.4.2 rollout completed on 5 pods", "{\"pods\":5}"),
                new SyntheticLog("LOG-chk-003", "checkout-service", Instant.parse("2026-10-15T10:17:12Z"), "ERROR", "NullPointerException: Cannot invoke DiscountCalculator.applyDiscount() because discountService is null at CartProcessor.process(CartProcessor.java:142)", "{\"orderId\":\"order-1093\",\"exception\":\"NullPointerException\"}"),
                new SyntheticLog("LOG-chk-004", "checkout-service", Instant.parse("2026-10-15T10:18:22Z"), "ERROR", "NullPointerException: Cannot invoke DiscountCalculator.applyDiscount() because discountService is null at CartProcessor.process(CartProcessor.java:142)", "{\"orderId\":\"order-1094\",\"exception\":\"NullPointerException\"}"),
                new SyntheticLog("LOG-chk-005", "checkout-service", Instant.parse("2026-10-15T10:19:40Z"), "WARN", "Circuit breaker HALF_OPEN for service payment-service", "{\"downstream\":\"payment-service\"}"),
                new SyntheticLog("LOG-chk-006", "checkout-service", Instant.parse("2026-10-15T10:20:15Z"), "ERROR", "HTTP 504 Gateway Timeout calling payment-service /api/v1/charge after 5000ms", "{\"endpoint\":\"/api/v1/charge\",\"durationMs\":5002}"),
                new SyntheticLog("LOG-pay-001", "payment-service", Instant.parse("2026-10-15T10:19:50Z"), "WARN", "Connection pool HikariPool-Payment reaching 95% utilization", "{\"activeConnections\":48,\"maxConnections\":50}"),
                new SyntheticLog("LOG-pay-002", "payment-service", Instant.parse("2026-10-15T10:20:10Z"), "ERROR", "TimeoutException: Connection acquisition timed out after 5000ms from database pool HikariPool", "{\"exception\":\"ConnectionTimeout\"}"),
                new SyntheticLog("LOG-inv-001", "inventory-service", Instant.parse("2026-10-15T10:10:00Z"), "INFO", "Inventory reservation succeeded for sku-4829", "{\"sku\":\"sku-4829\"}")
        ));

        // 4. Metrics
        metricRepo.saveAll(List.of(
                new SyntheticMetric("MET-chk-err-1010", "checkout-service", "error_rate", 0.01, "ratio", Instant.parse("2026-10-15T10:10:00Z")),
                new SyntheticMetric("MET-chk-err-1015", "checkout-service", "error_rate", 0.02, "ratio", Instant.parse("2026-10-15T10:15:00Z")),
                new SyntheticMetric("MET-chk-err-1020", "checkout-service", "error_rate", 0.18, "ratio", Instant.parse("2026-10-15T10:20:00Z")),
                new SyntheticMetric("MET-chk-err-1025", "checkout-service", "error_rate", 0.22, "ratio", Instant.parse("2026-10-15T10:25:00Z")),
                new SyntheticMetric("MET-chk-lat-1010", "checkout-service", "latency_p99", 120.0, "ms", Instant.parse("2026-10-15T10:10:00Z")),
                new SyntheticMetric("MET-chk-lat-1020", "checkout-service", "latency_p99", 2450.0, "ms", Instant.parse("2026-10-15T10:20:00Z")),
                new SyntheticMetric("MET-pay-err-1015", "payment-service", "error_rate", 0.005, "ratio", Instant.parse("2026-10-15T10:15:00Z")),
                new SyntheticMetric("MET-pay-err-1020", "payment-service", "error_rate", 0.14, "ratio", Instant.parse("2026-10-15T10:20:00Z")),
                new SyntheticMetric("MET-pay-lat-1020", "payment-service", "latency_p99", 5100.0, "ms", Instant.parse("2026-10-15T10:20:00Z")),
                new SyntheticMetric("MET-inv-err-1020", "inventory-service", "error_rate", 0.001, "ratio", Instant.parse("2026-10-15T10:20:00Z"))
        ));

        log.info("Synthetic operational data seeding completed");
    }
}
