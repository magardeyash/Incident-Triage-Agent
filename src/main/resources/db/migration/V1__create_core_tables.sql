CREATE TABLE IF NOT EXISTS alerts (
    id BIGSERIAL PRIMARY KEY,
    service_name VARCHAR(255) NOT NULL,
    severity VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    payload TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS investigations (
    id BIGSERIAL PRIMARY KEY,
    alert_id BIGINT NOT NULL REFERENCES alerts(id),
    status VARCHAR(50) NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE,
    finished_at TIMESTAMP WITH TIME ZONE,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS investigation_reports (
    id BIGSERIAL PRIMARY KEY,
    investigation_id BIGINT NOT NULL REFERENCES investigations(id),
    summary TEXT NOT NULL,
    probable_root_cause VARCHAR(100) NOT NULL,
    confidence DOUBLE PRECISION NOT NULL,
    evidence TEXT NOT NULL,
    recommendations TEXT NOT NULL,
    limitations TEXT,
    model_name VARCHAR(100) NOT NULL,
    prompt_version VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_alerts_service_occurred ON alerts(service_name, occurred_at);
CREATE INDEX IF NOT EXISTS idx_investigations_alert_id ON investigations(alert_id);
CREATE INDEX IF NOT EXISTS idx_investigation_reports_inv_id ON investigation_reports(investigation_id);
