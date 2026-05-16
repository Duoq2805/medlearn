-- =============================================
-- MEDICAL LEARNING SUPPORT SYSTEM
-- PostgreSQL Database Schema - Version 1.0 (Final - Fixed)
-- =============================================

-- =============================================
-- ENUM TYPES
-- =============================================
CREATE TYPE disease_status AS ENUM ('draft', 'pending_review', 'approved', 'archived');
CREATE TYPE version_status AS ENUM ('draft', 'pending_review', 'approved', 'rejected', 'archived');
CREATE TYPE user_status AS ENUM ('pending', 'active', 'banned');
CREATE TYPE case_difficulty AS ENUM ('easy', 'medium', 'hard');
CREATE TYPE audit_action AS ENUM ('CREATE', 'UPDATE', 'DELETE', 'SUBMIT', 'APPROVE', 'REJECT', 'LOGIN', 'LOGOUT');

-- =============================================
-- AUTO UPDATE TIMESTAMP FUNCTION
-- =============================================
CREATE OR REPLACE FUNCTION trigger_set_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- =============================================
-- SECTION TYPE (Không phụ thuộc bảng nào)
-- =============================================
CREATE TABLE section_type (
                              id          SERIAL PRIMARY KEY,
                              name        VARCHAR(100) UNIQUE NOT NULL,
                              description TEXT,
                              created_at  TIMESTAMPTZ DEFAULT NOW()
);

-- =============================================
-- SYMPTOM (Không phụ thuộc bảng nào)
-- =============================================
CREATE TABLE symptom (
                         id          BIGSERIAL PRIMARY KEY,
                         name        VARCHAR(255) NOT NULL,
                         slug        VARCHAR(255) UNIQUE NOT NULL,
                         description TEXT,
                         created_at  TIMESTAMPTZ DEFAULT NOW(),
                         updated_at  TIMESTAMPTZ DEFAULT NOW()
);

-- =============================================
-- USER (Không phụ thuộc bảng nào)
-- =============================================
CREATE TABLE "user" (
                        id              BIGSERIAL PRIMARY KEY,
                        username        VARCHAR(100) UNIQUE NOT NULL,
                        email           VARCHAR(255) UNIQUE NOT NULL,
                        password_hash   VARCHAR(255) NOT NULL,
                        full_name       VARCHAR(255),
                        avatar_url      TEXT,
                        phone_number    VARCHAR(20),
                        status          user_status DEFAULT 'pending',
                        last_login_at   TIMESTAMPTZ,
                        created_at      TIMESTAMPTZ DEFAULT NOW(),
                        updated_at      TIMESTAMPTZ DEFAULT NOW(),
                        is_deleted      BOOLEAN DEFAULT FALSE
);

-- =============================================
-- ROLE & USER_ROLE
-- =============================================
CREATE TABLE role (
                      id          SERIAL PRIMARY KEY,
                      name        VARCHAR(50) UNIQUE NOT NULL,
                      description TEXT
);

CREATE TABLE user_role (
                           user_id     BIGINT NOT NULL,
                           role_id     INT NOT NULL,
                           PRIMARY KEY (user_id, role_id),
                           CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
                           CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES role(id)
);

-- =============================================
-- DISEASE (tạo trước version, nhưng chưa có FK đến version)
-- =============================================
CREATE TABLE disease (
                         id                  BIGSERIAL PRIMARY KEY,
                         name                VARCHAR(255) NOT NULL,
                         slug                VARCHAR(255) UNIQUE NOT NULL,
                         current_version_id  BIGINT NULL,
                         status              disease_status DEFAULT 'draft',
                         created_at          TIMESTAMPTZ DEFAULT NOW(),
                         updated_at          TIMESTAMPTZ DEFAULT NOW(),
                         is_deleted          BOOLEAN DEFAULT FALSE
);

-- =============================================
-- DISEASE VERSION (có FK đến disease và user)
-- =============================================
CREATE TABLE disease_version (
                                 id                  BIGSERIAL PRIMARY KEY,
                                 disease_id          BIGINT NOT NULL,
                                 version_number      INT NOT NULL,
                                 created_by          BIGINT NOT NULL,
                                 status              version_status DEFAULT 'draft',
                                 review_note         TEXT,
                                 approved_by         BIGINT NULL,
                                 approved_at         TIMESTAMPTZ NULL,
                                 is_current          BOOLEAN DEFAULT FALSE,
                                 is_deleted          BOOLEAN DEFAULT FALSE,
                                 created_at          TIMESTAMPTZ DEFAULT NOW(),
                                 updated_at          TIMESTAMPTZ DEFAULT NOW(),
                                 CONSTRAINT fk_dv_disease FOREIGN KEY (disease_id) REFERENCES disease(id) ON DELETE CASCADE,
                                 CONSTRAINT fk_dv_created_by FOREIGN KEY (created_by) REFERENCES "user"(id),
                                 CONSTRAINT fk_dv_approved_by FOREIGN KEY (approved_by) REFERENCES "user"(id),
                                 CONSTRAINT uk_dv_disease_version UNIQUE (disease_id, version_number)
);

-- =============================================
-- DISEASE SECTION
-- =============================================
CREATE TABLE disease_section (
                                 id                  BIGSERIAL PRIMARY KEY,
                                 disease_version_id  BIGINT NOT NULL,
                                 section_type_id     INT NOT NULL,
                                 title               VARCHAR(255) NOT NULL,
                                 content             TEXT,
                                 order_index         INT NOT NULL DEFAULT 0,
                                 created_at          TIMESTAMPTZ DEFAULT NOW(),
                                 updated_at          TIMESTAMPTZ DEFAULT NOW(),
                                 is_deleted          BOOLEAN DEFAULT FALSE,
                                 CONSTRAINT fk_ds_disease_version FOREIGN KEY (disease_version_id) REFERENCES disease_version(id) ON DELETE CASCADE,
                                 CONSTRAINT fk_ds_section_type FOREIGN KEY (section_type_id) REFERENCES section_type(id)
);

-- =============================================
-- DISEASE VERSION SYMPTOM
-- =============================================
CREATE TABLE disease_version_symptom (
                                         id                  BIGSERIAL PRIMARY KEY,
                                         disease_version_id  BIGINT NOT NULL,
                                         symptom_id          BIGINT NOT NULL,
                                         weight_score        DECIMAL(5,2) DEFAULT 1.0,
                                         created_at          TIMESTAMPTZ DEFAULT NOW(),
                                         CONSTRAINT fk_dvs_disease_version FOREIGN KEY (disease_version_id) REFERENCES disease_version(id) ON DELETE CASCADE,
                                         CONSTRAINT fk_dvs_symptom FOREIGN KEY (symptom_id) REFERENCES symptom(id),
                                         CONSTRAINT uk_dvs UNIQUE (disease_version_id, symptom_id)
);

-- =============================================
-- CASE STUDY
-- =============================================
CREATE TABLE case_study (
                            id              BIGSERIAL PRIMARY KEY,
                            title           VARCHAR(255) NOT NULL,
                            slug            VARCHAR(255) UNIQUE NOT NULL,
                            description     TEXT,
                            difficulty      case_difficulty DEFAULT 'medium',
                            diagnosis       TEXT,
                            learning_notes  TEXT,
                            created_by      BIGINT NOT NULL,
                            status          disease_status DEFAULT 'draft',
                            created_at      TIMESTAMPTZ DEFAULT NOW(),
                            updated_at      TIMESTAMPTZ DEFAULT NOW(),
                            is_deleted      BOOLEAN DEFAULT FALSE,
                            CONSTRAINT fk_cs_created_by FOREIGN KEY (created_by) REFERENCES "user"(id)
);

CREATE TABLE case_study_symptom (
                                    case_study_id   BIGINT NOT NULL,
                                    symptom_id      BIGINT NOT NULL,
                                    PRIMARY KEY (case_study_id, symptom_id),
                                    CONSTRAINT fk_css_case FOREIGN KEY (case_study_id) REFERENCES case_study(id) ON DELETE CASCADE,
                                    CONSTRAINT fk_css_symptom FOREIGN KEY (symptom_id) REFERENCES symptom(id)
);

-- =============================================
-- AUDIT LOG
-- =============================================
CREATE TABLE audit_log (
                           id          BIGSERIAL PRIMARY KEY,
                           user_id     BIGINT NOT NULL,
                           action_type audit_action NOT NULL,
                           entity_name VARCHAR(100) NOT NULL,
                           entity_id   BIGINT NOT NULL,
                           old_data    JSONB,
                           new_data    JSONB,
                           reason      TEXT,
                           created_at  TIMESTAMPTZ DEFAULT NOW(),
                           CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES "user"(id)
);

-- =============================================
-- SAU KHI TẤT CẢ BẢNG ĐƯỢC TẠO XONG, THÊM FK CHO DISEASE
-- =============================================
ALTER TABLE disease
    ADD CONSTRAINT fk_disease_current_version
        FOREIGN KEY (current_version_id) REFERENCES disease_version(id);

-- =============================================
-- TRIGGERS
-- =============================================
CREATE TRIGGER set_timestamp_disease
    BEFORE UPDATE ON disease
    FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();

CREATE TRIGGER set_timestamp_disease_version
    BEFORE UPDATE ON disease_version
    FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();

CREATE TRIGGER set_timestamp_disease_section
    BEFORE UPDATE ON disease_section
    FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();

CREATE TRIGGER set_timestamp_symptom
    BEFORE UPDATE ON symptom
    FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();

CREATE TRIGGER set_timestamp_user
    BEFORE UPDATE ON "user"
    FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();

CREATE TRIGGER set_timestamp_case_study
    BEFORE UPDATE ON case_study
    FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();

-- =============================================
-- INDEXES
-- =============================================
CREATE INDEX idx_disease_slug ON disease(slug);
CREATE INDEX idx_disease_status ON disease(status);
CREATE INDEX idx_disease_current_version ON disease(current_version_id);

CREATE INDEX idx_dv_disease_id ON disease_version(disease_id);
CREATE INDEX idx_dv_status ON disease_version(status);
CREATE INDEX idx_dv_is_current ON disease_version(is_current);

CREATE INDEX idx_ds_version_id ON disease_section(disease_version_id);
CREATE INDEX idx_ds_order ON disease_section(order_index);

CREATE INDEX idx_symptom_slug ON symptom(slug);
CREATE INDEX idx_dvs_symptom ON disease_version_symptom(symptom_id);

CREATE INDEX idx_user_email ON "user"(email);
CREATE INDEX idx_audit_log_entity ON audit_log(entity_name, entity_id);
CREATE INDEX idx_audit_log_created ON audit_log(created_at);

COMMENT ON DATABASE medlearn_db IS 'Medical Learning Support System - Educational Purpose Only';
COMMENT ON FUNCTION trigger_set_timestamp() IS 'Tự động cập nhật cột updated_at cho tất cả bảng';

ALTER TABLE disease_version ADD COLUMN version INT DEFAULT 0;
ALTER TABLE disease ADD COLUMN version INT DEFAULT 0;