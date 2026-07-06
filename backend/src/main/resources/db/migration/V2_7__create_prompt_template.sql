-- Prompt template table
CREATE TABLE prompt_template (
    id                 BIGSERIAL    PRIMARY KEY,
    code               VARCHAR(100) NOT NULL,
    name               VARCHAR(255) NOT NULL,
    description        TEXT,
    system_prompt      TEXT         NOT NULL,
    user_prompt_template TEXT       NOT NULL,
    version            VARCHAR(20)  NOT NULL DEFAULT '1.0',
    model              VARCHAR(100),
    temperature        REAL,
    max_tokens         INTEGER,
    required_variables TEXT,
    status             VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_by         BIGINT       REFERENCES users(id) ON DELETE SET NULL,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_prompt_template_code_version UNIQUE (code, version)
);

CREATE INDEX idx_prompt_template_code ON prompt_template(code);
