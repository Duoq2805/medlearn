-- AI response cache table
CREATE TABLE ai_cache (
    id              BIGSERIAL    PRIMARY KEY,
    cache_key       VARCHAR(255) NOT NULL UNIQUE,
    request_hash    VARCHAR(64)  NOT NULL,
    prompt_code     VARCHAR(100),
    model           VARCHAR(100),
    response_body   TEXT         NOT NULL,
    prompt_tokens   INTEGER      NOT NULL DEFAULT 0,
    completion_tokens INTEGER    NOT NULL DEFAULT 0,
    total_tokens    INTEGER      NOT NULL DEFAULT 0,
    ttl_minutes     INTEGER      NOT NULL DEFAULT 60,
    expires_at      TIMESTAMPTZ  NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_ai_cache_request UNIQUE (request_hash, model)
);

CREATE INDEX idx_ai_cache_expires_at  ON ai_cache(expires_at);
CREATE INDEX idx_ai_cache_cache_key   ON ai_cache(cache_key);
CREATE INDEX idx_ai_cache_prompt_code ON ai_cache(prompt_code);
