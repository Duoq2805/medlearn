-- AI usage log table
CREATE TABLE ai_usage_log (
    id               BIGSERIAL    PRIMARY KEY,
    user_id          BIGINT       REFERENCES users(id) ON DELETE SET NULL,
    request_type     VARCHAR(50)  NOT NULL,
    model            VARCHAR(100),
    provider         VARCHAR(50),
    prompt_tokens    INTEGER      NOT NULL DEFAULT 0,
    completion_tokens INTEGER     NOT NULL DEFAULT 0,
    total_tokens     INTEGER      NOT NULL DEFAULT 0,
    latency_ms       INTEGER,
    success          BOOLEAN      NOT NULL DEFAULT TRUE,
    error_message    TEXT,
    cached           BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ai_usage_user_id    ON ai_usage_log(user_id);
CREATE INDEX idx_ai_usage_created_at ON ai_usage_log(created_at);
CREATE INDEX idx_ai_usage_request_type ON ai_usage_log(request_type);
