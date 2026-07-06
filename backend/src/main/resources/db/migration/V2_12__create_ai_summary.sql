-- AI Summary table for storing AI-generated disease summaries
-- Each summary is linked to a disease and a specific version
-- Supports multiple summary types (STUDENT, CLINICAL, EXAM, QUICK_REVISION)
-- Uses soft delete for data retention

CREATE TABLE ai_summary (
    id              BIGSERIAL       PRIMARY KEY,
    disease_id      BIGINT          NOT NULL,
    disease_version_id BIGINT       NOT NULL,
    summary_type    VARCHAR(50)     NOT NULL,
    version         INTEGER         NOT NULL DEFAULT 1,
    content         TEXT            NOT NULL,
    model           VARCHAR(100),
    provider        VARCHAR(50),
    prompt_tokens   INTEGER         NOT NULL DEFAULT 0,
    completion_tokens INTEGER       NOT NULL DEFAULT 0,
    total_tokens    INTEGER         NOT NULL DEFAULT 0,
    latency_ms      INTEGER,
    created_by      BIGINT,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE,
    deleted_at      TIMESTAMP WITH TIME ZONE,

    CONSTRAINT fk_ai_summary_disease
        FOREIGN KEY (disease_id) REFERENCES disease(id),
    CONSTRAINT fk_ai_summary_disease_version
        FOREIGN KEY (disease_version_id) REFERENCES disease_version(id),
    CONSTRAINT fk_ai_summary_created_by
        FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT uq_ai_summary_disease_type_version
        UNIQUE (disease_id, summary_type, version)
);

CREATE INDEX idx_ai_summary_disease_id ON ai_summary(disease_id);
CREATE INDEX idx_ai_summary_disease_version_id ON ai_summary(disease_version_id);
CREATE INDEX idx_ai_summary_summary_type ON ai_summary(summary_type);
CREATE INDEX idx_ai_summary_created_at ON ai_summary(created_at);
