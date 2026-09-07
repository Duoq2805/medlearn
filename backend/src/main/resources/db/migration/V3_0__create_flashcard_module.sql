-- =========================================
-- FLASHCARD MODULE — V3.0
-- AI-powered flashcard generation, SM-2 review, shared AI infrastructure
-- =========================================

-- =========================================
-- AI GENERATION (shared — Summary, Flashcard, Quiz, Case Study, Chat)
-- =========================================
CREATE TABLE ai_generation (
    id BIGSERIAL PRIMARY KEY,

    feature_type VARCHAR(30) NOT NULL,
    feature_id BIGINT NOT NULL,

    prompt_template_code VARCHAR(100),
    prompt_template_version VARCHAR(20),

    model VARCHAR(100),
    provider VARCHAR(50),

    prompt_tokens INT NOT NULL DEFAULT 0,
    completion_tokens INT NOT NULL DEFAULT 0,
    total_tokens INT NOT NULL DEFAULT 0,
    latency_ms INT,

    system_prompt TEXT,
    user_prompt TEXT,
    raw_response TEXT,

    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    error_message TEXT,

    metadata TEXT,

    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_ai_generation_feature ON ai_generation(feature_type, feature_id);
CREATE INDEX idx_ai_generation_created_at ON ai_generation(created_at);

-- =========================================
-- AI FEEDBACK (shared — rate any AI-generated content)
-- =========================================
CREATE TABLE ai_feedback (
    id BIGSERIAL PRIMARY KEY,

    feature_type VARCHAR(30) NOT NULL,
    feature_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(id),

    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    response_quality VARCHAR(20),

    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),

    UNIQUE (feature_type, feature_id, user_id)
);

CREATE INDEX idx_ai_feedback_feature ON ai_feedback(feature_type, feature_id);
CREATE INDEX idx_ai_feedback_user ON ai_feedback(user_id);

-- =========================================
-- FLASHCARD DECK (aggregate root)
-- =========================================
CREATE TABLE flashcard_deck (
    id BIGSERIAL PRIMARY KEY,

    title VARCHAR(255) NOT NULL,
    description TEXT,

    source_type VARCHAR(30) NOT NULL,
    source_id BIGINT NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    card_count INT NOT NULL DEFAULT 0,

    created_by BIGINT NOT NULL REFERENCES users(id),

    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX idx_flashcard_deck_source ON flashcard_deck(source_type, source_id);
CREATE INDEX idx_flashcard_deck_created_by ON flashcard_deck(created_by);
CREATE INDEX idx_flashcard_deck_status ON flashcard_deck(status);
CREATE INDEX idx_flashcard_deck_deleted ON flashcard_deck(deleted_at);

CREATE TRIGGER trg_flashcard_deck_updated_at
    BEFORE UPDATE ON flashcard_deck
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- =========================================
-- FLASHCARD
-- =========================================
CREATE TABLE flashcard (
    id BIGSERIAL PRIMARY KEY,

    deck_id BIGINT NOT NULL REFERENCES flashcard_deck(id) ON DELETE CASCADE,

    question TEXT NOT NULL,
    answer TEXT NOT NULL,
    explanation TEXT,

    source VARCHAR(500),
    tag VARCHAR(100),

    difficulty VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_by BIGINT NOT NULL REFERENCES users(id),

    total_reviews INT NOT NULL DEFAULT 0,
    correct_count INT NOT NULL DEFAULT 0,
    incorrect_count INT NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX idx_flashcard_deck_id ON flashcard(deck_id);
CREATE INDEX idx_flashcard_status ON flashcard(status);
CREATE INDEX idx_flashcard_tag ON flashcard(tag);
CREATE INDEX idx_flashcard_created_by ON flashcard(created_by);
CREATE INDEX idx_flashcard_deleted ON flashcard(deleted_at);

CREATE TRIGGER trg_flashcard_updated_at
    BEFORE UPDATE ON flashcard
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- =========================================
-- FLASHCARD SOURCE (join table: flashcard ↔ document chunk)
-- =========================================
CREATE TABLE flashcard_source (
    id BIGSERIAL PRIMARY KEY,

    flashcard_id BIGINT NOT NULL REFERENCES flashcard(id) ON DELETE CASCADE,
    source_type VARCHAR(30) NOT NULL,
    source_id BIGINT NOT NULL,

    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_flashcard_source_card ON flashcard_source(flashcard_id);
CREATE INDEX idx_flashcard_source_target ON flashcard_source(source_type, source_id);

-- =========================================
-- FLASHCARD PROGRESS (per-user SM-2 state)
-- =========================================
CREATE TABLE flashcard_progress (
    id BIGSERIAL PRIMARY KEY,

    flashcard_id BIGINT NOT NULL REFERENCES flashcard(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id),

    easiness_factor DOUBLE PRECISION NOT NULL DEFAULT 2.5,
    review_interval INT NOT NULL DEFAULT 0,
    repetitions INT NOT NULL DEFAULT 0,

    next_review_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_reviewed_at TIMESTAMPTZ,
    last_quality INT,

    total_reviews INT NOT NULL DEFAULT 0,
    correct_count INT NOT NULL DEFAULT 0,
    incorrect_count INT NOT NULL DEFAULT 0,

    mastered BOOLEAN NOT NULL DEFAULT FALSE,

    version INT NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,

    UNIQUE (flashcard_id, user_id)
);

CREATE INDEX idx_progress_next_review ON flashcard_progress(next_review_at);
CREATE INDEX idx_progress_user ON flashcard_progress(user_id);
CREATE INDEX idx_progress_deleted ON flashcard_progress(deleted_at);

CREATE TRIGGER trg_flashcard_progress_updated_at
    BEFORE UPDATE ON flashcard_progress
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- =========================================
-- FLASHCARD REVIEW LOG (audit trail)
-- =========================================
CREATE TABLE flashcard_review_log (
    id BIGSERIAL PRIMARY KEY,

    flashcard_id BIGINT NOT NULL REFERENCES flashcard(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id),

    quality INT NOT NULL CHECK (quality >= 0 AND quality <= 5),
    response_time_ms INT,

    reviewed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    ef_before DOUBLE PRECISION,
    ef_after DOUBLE PRECISION,
    interval_before INT,
    interval_after INT,

    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_review_log_card ON flashcard_review_log(flashcard_id);
CREATE INDEX idx_review_log_user_date ON flashcard_review_log(user_id, reviewed_at);

-- =========================================
-- SEED FLASHCARD PERMISSIONS
-- =========================================
INSERT INTO permission(name, description, category)
SELECT v.name, v.description, v.category
FROM (VALUES
    ('FLASHCARD_GENERATE', 'Generate flashcards using AI', 'FLASHCARD'),
    ('FLASHCARD_VIEW', 'View flashcards and decks', 'FLASHCARD'),
    ('FLASHCARD_REVIEW', 'Review flashcards and track progress', 'FLASHCARD'),
    ('FLASHCARD_EDIT', 'Edit flashcard content', 'FLASHCARD'),
    ('FLASHCARD_DELETE', 'Delete own flashcards', 'FLASHCARD'),
    ('FLASHCARD_MANAGE', 'Admin: manage all flashcards', 'FLASHCARD'),
    ('FLASHCARD_EXPORT', 'Export flashcards', 'FLASHCARD')
) AS v(name, description, category)
WHERE NOT EXISTS (SELECT 1 FROM permission p WHERE p.name = v.name);

-- USER role gets GENERATE + VIEW + REVIEW + DELETE + EXPORT
INSERT INTO role_permission(role_id, permission_id)
SELECT r.id, p.id
FROM role r, permission p
WHERE r.name = 'USER'
AND p.name IN (
    'FLASHCARD_GENERATE',
    'FLASHCARD_VIEW',
    'FLASHCARD_REVIEW',
    'FLASHCARD_EDIT',
    'FLASHCARD_DELETE',
    'FLASHCARD_EXPORT'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permission rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- REVIEWER gets everything USER has (already handled by earlier grants)
-- plus FLASHCARD_MANAGE for reviewing
INSERT INTO role_permission(role_id, permission_id)
SELECT r.id, p.id
FROM role r, permission p
WHERE r.name = 'REVIEWER'
AND p.name IN ('FLASHCARD_MANAGE')
AND NOT EXISTS (
    SELECT 1 FROM role_permission rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- ADMIN gets all 7
INSERT INTO role_permission(role_id, permission_id)
SELECT r.id, p.id
FROM role r, permission p
WHERE r.name = 'ADMIN'
AND p.name IN (
    'FLASHCARD_GENERATE',
    'FLASHCARD_VIEW',
    'FLASHCARD_REVIEW',
    'FLASHCARD_EDIT',
    'FLASHCARD_DELETE',
    'FLASHCARD_MANAGE',
    'FLASHCARD_EXPORT'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permission rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);
