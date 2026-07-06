-- Prompt test result table
CREATE TABLE prompt_test_result (
    id              BIGSERIAL    PRIMARY KEY,
    prompt_code     VARCHAR(100) NOT NULL,
    version         VARCHAR(20)  NOT NULL,
    variables       TEXT,
    expected_output TEXT,
    actual_output   TEXT,
    passed          BOOLEAN      NOT NULL,
    error_message   TEXT,
    latency_ms      INTEGER,
    executed_by     BIGINT       REFERENCES users(id) ON DELETE SET NULL,
    executed_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_test_prompt_template FOREIGN KEY (prompt_code, version)
        REFERENCES prompt_template(code, version) ON DELETE CASCADE
);

CREATE INDEX idx_test_prompt_code ON prompt_test_result(prompt_code);
CREATE INDEX idx_test_executed_at ON prompt_test_result(executed_at);
