CREATE TABLE IF NOT EXISTS synthetic_logs (
    id BIGSERIAL PRIMARY KEY,
    log_id VARCHAR(100) UNIQUE NOT NULL,
    service_name VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    level VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    metadata TEXT
);

CREATE TABLE IF NOT EXISTS synthetic_metrics (
    id BIGSERIAL PRIMARY KEY,
    metric_id VARCHAR(100) UNIQUE NOT NULL,
    service_name VARCHAR(255) NOT NULL,
    metric_name VARCHAR(100) NOT NULL,
    value DOUBLE PRECISION NOT NULL,
    unit VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS synthetic_deployments (
    id BIGSERIAL PRIMARY KEY,
    deployment_id VARCHAR(100) UNIQUE NOT NULL,
    service_name VARCHAR(255) NOT NULL,
    version VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    deployed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    commit_hash VARCHAR(100) NOT NULL,
    summary TEXT
);

CREATE TABLE IF NOT EXISTS synthetic_service_dependencies (
    id BIGSERIAL PRIMARY KEY,
    dependency_id VARCHAR(100) UNIQUE NOT NULL,
    source_service VARCHAR(255) NOT NULL,
    target_service VARCHAR(255) NOT NULL,
    dependency_type VARCHAR(50) NOT NULL,
    critical BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_synth_logs_svc_time ON synthetic_logs(service_name, timestamp);
CREATE INDEX IF NOT EXISTS idx_synth_metrics_svc_time ON synthetic_metrics(service_name, timestamp);
CREATE INDEX IF NOT EXISTS idx_synth_deploys_svc_time ON synthetic_deployments(service_name, deployed_at);
CREATE INDEX IF NOT EXISTS idx_synth_deps_src ON synthetic_service_dependencies(source_service);

-- Seed deterministic operational dependencies
INSERT INTO synthetic_service_dependencies (dependency_id, source_service, target_service, dependency_type, critical) VALUES
('DEPGRAPH-chk-pay', 'checkout-service', 'payment-service', 'SYNC_HTTP', true),
('DEPGRAPH-chk-inv', 'checkout-service', 'inventory-service', 'SYNC_HTTP', true),
('DEPGRAPH-pay-gw', 'payment-service', 'external-stripe-api', 'SYNC_HTTP', true),
('DEPGRAPH-inv-db', 'inventory-service', 'inventory-postgres', 'DATABASE', true)
ON CONFLICT (dependency_id) DO NOTHING;

-- Seed deterministic deployments
INSERT INTO synthetic_deployments (deployment_id, service_name, version, status, deployed_at, commit_hash, summary) VALUES
('DEP-chk-v1.4.2', 'checkout-service', 'v1.4.2', 'SUCCESS', '2026-10-15T10:15:00Z', 'c8f39a1', 'Feature: new cart checkout discounts module'),
('DEP-chk-v1.4.1', 'checkout-service', 'v1.4.1', 'SUCCESS', '2026-10-15T08:00:00Z', 'b4e12c9', 'Chore: bump logback version'),
('DEP-pay-v2.1.0', 'payment-service', 'v2.1.0', 'SUCCESS', '2026-10-15T09:30:00Z', 'a71b3e8', 'Fix: retry logic on stripe client'),
('DEP-inv-v1.1.0', 'inventory-service', 'v1.1.0', 'SUCCESS', '2026-10-14T18:00:00Z', '99a4bc1', 'Initial stable inventory service release')
ON CONFLICT (deployment_id) DO NOTHING;

-- Seed deterministic logs
INSERT INTO synthetic_logs (log_id, service_name, timestamp, level, message, metadata) VALUES
('LOG-chk-001', 'checkout-service', '2026-10-15T10:14:50Z', 'INFO', 'checkout-service v1.4.1 handling checkout request for order-1092', '{"orderId":"order-1092"}'),
('LOG-chk-002', 'checkout-service', '2026-10-15T10:16:05Z', 'INFO', 'checkout-service v1.4.2 rollout completed on 5 pods', '{"pods":5}'),
('LOG-chk-003', 'checkout-service', '2026-10-15T10:17:12Z', 'ERROR', 'NullPointerException: Cannot invoke DiscountCalculator.applyDiscount() because discountService is null at CartProcessor.process(CartProcessor.java:142)', '{"orderId":"order-1093","exception":"NullPointerException"}'),
('LOG-chk-004', 'checkout-service', '2026-10-15T10:18:22Z', 'ERROR', 'NullPointerException: Cannot invoke DiscountCalculator.applyDiscount() because discountService is null at CartProcessor.process(CartProcessor.java:142)', '{"orderId":"order-1094","exception":"NullPointerException"}'),
('LOG-chk-005', 'checkout-service', '2026-10-15T10:19:40Z', 'WARN', 'Circuit breaker HALF_OPEN for service payment-service', '{"downstream":"payment-service"}'),
('LOG-chk-006', 'checkout-service', '2026-10-15T10:20:15Z', 'ERROR', 'HTTP 504 Gateway Timeout calling payment-service /api/v1/charge after 5000ms', '{"endpoint":"/api/v1/charge","durationMs":5002}'),
('LOG-pay-001', 'payment-service', '2026-10-15T10:19:50Z', 'WARN', 'Connection pool HikariPool-Payment reaching 95% utilization', '{"activeConnections":48,"maxConnections":50}'),
('LOG-pay-002', 'payment-service', '2026-10-15T10:20:10Z', 'ERROR', 'TimeoutException: Connection acquisition timed out after 5000ms from database pool', '{"exception":"ConnectionTimeout"}'),
('LOG-inv-001', 'inventory-service', '2026-10-15T10:10:00Z', 'INFO', 'Inventory reservation succeeded for sku-4829', '{"sku":"sku-4829"}')
ON CONFLICT (log_id) DO NOTHING;

-- Seed deterministic metrics
INSERT INTO synthetic_metrics (metric_id, service_name, metric_name, value, unit, timestamp) VALUES
('MET-chk-err-1010', 'checkout-service', 'error_rate', 0.01, 'ratio', '2026-10-15T10:10:00Z'),
('MET-chk-err-1015', 'checkout-service', 'error_rate', 0.02, 'ratio', '2026-10-15T10:15:00Z'),
('MET-chk-err-1020', 'checkout-service', 'error_rate', 0.18, 'ratio', '2026-10-15T10:20:00Z'),
('MET-chk-err-1025', 'checkout-service', 'error_rate', 0.22, 'ratio', '2026-10-15T10:25:00Z'),
('MET-chk-lat-1010', 'checkout-service', 'latency_p99', 120.0, 'ms', '2026-10-15T10:10:00Z'),
('MET-chk-lat-1020', 'checkout-service', 'latency_p99', 2450.0, 'ms', '2026-10-15T10:20:00Z'),
('MET-pay-err-1015', 'payment-service', 'error_rate', 0.005, 'ratio', '2026-10-15T10:15:00Z'),
('MET-pay-err-1020', 'payment-service', 'error_rate', 0.14, 'ratio', '2026-10-15T10:20:00Z'),
('MET-pay-lat-1020', 'payment-service', 'latency_p99', 5100.0, 'ms', '2026-10-15T10:20:00Z'),
('MET-inv-err-1020', 'inventory-service', 'error_rate', 0.001, 'ratio', '2026-10-15T10:20:00Z')
ON CONFLICT (metric_id) DO NOTHING;
