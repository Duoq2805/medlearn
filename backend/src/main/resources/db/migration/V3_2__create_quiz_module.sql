-- =========================================
-- QUIZ MODULE — V3.2
-- AI-powered quiz generation, attempt tracking, answer grading
-- =========================================

-- =========================================
-- QUIZ (aggregate root)
-- =========================================
CREATE TABLE quiz (
    id BIGSERIAL PRIMARY KEY,

    title VARCHAR(255) NOT NULL,

    source_type VARCHAR(30) NOT NULL,
    source_id BIGINT NOT NULL,

    question_count INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_by BIGINT NOT NULL REFERENCES users(id),

    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX idx_quiz_created_by ON quiz(created_by);
CREATE INDEX idx_quiz_source ON quiz(source_type, source_id);
CREATE INDEX idx_quiz_deleted ON quiz(deleted_at);

CREATE TRIGGER trg_quiz_updated_at
    BEFORE UPDATE ON quiz
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- =========================================
-- QUIZ QUESTION
-- =========================================
CREATE TABLE quiz_question (
    id BIGSERIAL PRIMARY KEY,

    quiz_id BIGINT NOT NULL REFERENCES quiz(id) ON DELETE CASCADE,

    content TEXT NOT NULL,

    option_a TEXT NOT NULL,
    option_b TEXT NOT NULL,
    option_c TEXT NOT NULL,
    option_d TEXT NOT NULL,

    correct_answer VARCHAR(1) NOT NULL CHECK (correct_answer IN ('A','B','C','D')),
    explanation TEXT,

    display_order INT NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_quiz_question_quiz_id ON quiz_question(quiz_id);

-- =========================================
-- QUIZ ATTEMPT (per-user attempt record)
-- =========================================
CREATE TABLE quiz_attempt (
    id BIGSERIAL PRIMARY KEY,

    quiz_id BIGINT NOT NULL REFERENCES quiz(id),
    user_id BIGINT NOT NULL REFERENCES users(id),

    correct_count INT NOT NULL DEFAULT 0,
    total_questions INT NOT NULL DEFAULT 0,
    percentage DOUBLE PRECISION NOT NULL DEFAULT 0,

    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',

    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_quiz_attempt_user ON quiz_attempt(user_id);
CREATE INDEX idx_quiz_attempt_quiz ON quiz_attempt(quiz_id);

CREATE TRIGGER trg_quiz_attempt_updated_at
    BEFORE UPDATE ON quiz_attempt
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- =========================================
-- QUIZ ATTEMPT ANSWER
-- =========================================
CREATE TABLE quiz_attempt_answer (
    id BIGSERIAL PRIMARY KEY,

    attempt_id BIGINT NOT NULL REFERENCES quiz_attempt(id) ON DELETE CASCADE,
    question_id BIGINT NOT NULL REFERENCES quiz_question(id),

    selected_answer VARCHAR(1) CHECK (selected_answer IN ('A','B','C','D')),
    correct_answer VARCHAR(1) NOT NULL CHECK (correct_answer IN ('A','B','C','D')),
    is_correct BOOLEAN NOT NULL,

    created_at TIMESTAMPTZ DEFAULT NOW(),

    UNIQUE (attempt_id, question_id)
);

CREATE INDEX idx_quiz_answer_attempt ON quiz_attempt_answer(attempt_id);

-- =========================================
-- SEED QUIZ PERMISSIONS
-- =========================================
INSERT INTO permission(name, description, category)
SELECT v.name, v.description, v.category
FROM (VALUES
    ('QUIZ_GENERATE', 'Generate quizzes using AI', 'QUIZ'),
    ('QUIZ_VIEW',     'View quizzes and history',  'QUIZ'),
    ('QUIZ_ATTEMPT',  'Submit quiz attempts',      'QUIZ'),
    ('QUIZ_MANAGE',   'Admin: manage all quizzes', 'QUIZ')
) AS v(name, description, category)
WHERE NOT EXISTS (SELECT 1 FROM permission p WHERE p.name = v.name);

-- USER role
INSERT INTO role_permission(role_id, permission_id)
SELECT r.id, p.id
FROM role r, permission p
WHERE r.name = 'USER'
  AND p.name IN ('QUIZ_GENERATE', 'QUIZ_VIEW', 'QUIZ_ATTEMPT')
  AND NOT EXISTS (
    SELECT 1 FROM role_permission rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- REVIEWER role
INSERT INTO role_permission(role_id, permission_id)
SELECT r.id, p.id
FROM role r, permission p
WHERE r.name = 'REVIEWER'
  AND p.name IN ('QUIZ_MANAGE')
  AND NOT EXISTS (
    SELECT 1 FROM role_permission rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- ADMIN role
INSERT INTO role_permission(role_id, permission_id)
SELECT r.id, p.id
FROM role r, permission p
WHERE r.name = 'ADMIN'
  AND p.name IN ('QUIZ_GENERATE', 'QUIZ_VIEW', 'QUIZ_ATTEMPT', 'QUIZ_MANAGE')
  AND NOT EXISTS (
    SELECT 1 FROM role_permission rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- NOTE: quiz-gen prompt is defined in source code (QuizGenerationPrompt.java)
-- and is NOT seeded to DB. Prompts for AI generation features are application
-- code, version-controlled in Git, not business data.
