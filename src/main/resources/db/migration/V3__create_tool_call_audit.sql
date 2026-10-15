CREATE TABLE IF NOT EXISTS tool_call_audits (
    id BIGSERIAL PRIMARY KEY,
    investigation_id BIGINT NOT NULL,
    tool_name VARCHAR(100) NOT NULL,
    sanitized_arguments TEXT,
    result_summary TEXT,
    duration_ms BIGINT NOT NULL,
    success BOOLEAN NOT NULL,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tool_calls_inv_id ON tool_call_audits(investigation_id);
