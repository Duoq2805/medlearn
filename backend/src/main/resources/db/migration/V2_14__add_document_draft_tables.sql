CREATE EXTENSION IF NOT EXISTS vector;

-- =========================================
-- DOCUMENT MANAGEMENT & DRAFT GENERATION
-- Tables for document upload, chunking, and AI-powered disease draft generation
-- =========================================

-- =========================================
-- DOCUMENTS
-- =========================================
CREATE TABLE document (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL DEFAULT 0,
    mime_type VARCHAR(127),
    storage_path VARCHAR(500) NOT NULL,
    source_url VARCHAR(1000),
    checksum VARCHAR(64),
    page_count INT,
    status VARCHAR(20) NOT NULL DEFAULT 'UPLOADED',
    error_message TEXT,
    created_by BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX idx_document_created_by ON document(created_by);
CREATE INDEX idx_document_status ON document(status);
CREATE INDEX idx_document_deleted_at ON document(deleted_at);

-- Document chunks (extracted text segments)
CREATE TABLE document_chunk (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES document(id) ON DELETE CASCADE,
    chunk_index INT NOT NULL,
    content TEXT NOT NULL,
    char_count INT NOT NULL DEFAULT 0,
    page_number INT,
    heading VARCHAR(255),
    embedding vector(1536),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX idx_document_chunk_doc ON document_chunk(document_id);
CREATE INDEX idx_document_chunk_deleted ON document_chunk(deleted_at);

-- =========================================
-- DISEASE DRAFTS (AI-generated draft proposals)
-- =========================================
CREATE TABLE disease_draft (
    id BIGSERIAL PRIMARY KEY,
    disease_id BIGINT REFERENCES disease(id),
    title VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    source_method VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    source_document_id BIGINT REFERENCES document(id),
    ai_model VARCHAR(50),
    ai_prompt_template_code VARCHAR(100),
    ai_total_tokens INT DEFAULT 0,
    ai_latency_ms INT DEFAULT 0,
    review_note TEXT,
    created_by BIGINT NOT NULL REFERENCES users(id),
    reviewed_by BIGINT REFERENCES users(id),
    reviewed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX idx_disease_draft_disease ON disease_draft(disease_id);
CREATE INDEX idx_disease_draft_status ON disease_draft(status);
CREATE INDEX idx_disease_draft_created_by ON disease_draft(created_by);
CREATE INDEX idx_disease_draft_deleted ON disease_draft(deleted_at);

-- Draft sections (generated content for each disease section)
CREATE TABLE disease_draft_section (
    id BIGSERIAL PRIMARY KEY,
    draft_id BIGINT NOT NULL REFERENCES disease_draft(id) ON DELETE CASCADE,
    section_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    order_index INT NOT NULL DEFAULT 0,
    word_count INT DEFAULT 0,
    ai_generated BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX idx_draft_section_draft ON disease_draft_section(draft_id);
CREATE INDEX idx_draft_section_deleted ON disease_draft_section(deleted_at);

-- Draft sources (tracks which document chunks contributed to which sections)
CREATE TABLE draft_source (
    id BIGSERIAL PRIMARY KEY,
    draft_section_id BIGINT NOT NULL REFERENCES disease_draft_section(id) ON DELETE CASCADE,
    document_chunk_id BIGINT NOT NULL REFERENCES document_chunk(id) ON DELETE CASCADE,
    relevance_score FLOAT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_draft_source_section ON draft_source(draft_section_id);
CREATE INDEX idx_draft_source_chunk ON draft_source(document_chunk_id);

-- Draft provenance (full prompt/response trace for audit)
CREATE TABLE draft_provenance (
    id BIGSERIAL PRIMARY KEY,
    draft_id BIGINT NOT NULL REFERENCES disease_draft(id) ON DELETE CASCADE,
    section_type VARCHAR(50) NOT NULL,
    system_prompt TEXT,
    user_prompt TEXT NOT NULL,
    raw_response TEXT NOT NULL,
    model VARCHAR(50),
    provider VARCHAR(50),
    prompt_tokens INT DEFAULT 0,
    completion_tokens INT DEFAULT 0,
    total_tokens INT DEFAULT 0,
    latency_ms INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_draft_provenance_draft ON draft_provenance(draft_id);

-- =========================================
-- SEED DOCUMENT & DRAFT PERMISSIONS
-- Follows V1_5 pattern: insert into permission table then map to roles
-- =========================================
INSERT INTO permission(name, description, category) VALUES
-- Document domain (4)
('DOCUMENT_UPLOAD', 'Upload and process documents', 'DOCUMENT'),
('DOCUMENT_VIEW', 'View uploaded documents', 'DOCUMENT'),
('DOCUMENT_DELETE', 'Delete uploaded documents', 'DOCUMENT'),
('DOCUMENT_MANAGE', 'Manage all documents', 'DOCUMENT'),

-- Draft domain (5)
('DRAFT_CREATE', 'Create and edit drafts', 'DRAFT'),
('DRAFT_VIEW', 'View drafts', 'DRAFT'),
('DRAFT_DELETE', 'Delete drafts', 'DRAFT'),
('DRAFT_REVIEW', 'Review and approve/reject drafts', 'DRAFT'),
('AI_DRAFT', 'Generate drafts using AI', 'DRAFT');

-- USER role (4 permissions)
INSERT INTO role_permission(role_id, permission_id)
SELECT r.id, p.id
FROM role r, permission p
WHERE r.name = 'USER'
AND p.name IN (
    'DOCUMENT_UPLOAD',
    'DOCUMENT_VIEW',
    'DRAFT_CREATE',
    'DRAFT_VIEW'
);

-- REVIEWER role (USER + 3)
INSERT INTO role_permission(role_id, permission_id)
SELECT r.id, p.id
FROM role r, permission p
WHERE r.name = 'REVIEWER'
AND p.name IN (
    'DOCUMENT_UPLOAD',
    'DOCUMENT_VIEW',
    'DOCUMENT_DELETE',
    'DRAFT_CREATE',
    'DRAFT_VIEW',
    'DRAFT_REVIEW',
    'AI_DRAFT'
);

-- ADMIN role (all 9 permissions)
INSERT INTO role_permission(role_id, permission_id)
SELECT r.id, p.id
FROM role r, permission p
WHERE r.name = 'ADMIN'
AND p.name IN (
    'DOCUMENT_UPLOAD',
    'DOCUMENT_VIEW',
    'DOCUMENT_DELETE',
    'DOCUMENT_MANAGE',
    'DRAFT_CREATE',
    'DRAFT_VIEW',
    'DRAFT_DELETE',
    'DRAFT_REVIEW',
    'AI_DRAFT'
);
