-- =========================================
-- MEDLEARN DATABASE V1.0 CLEAN
-- =========================================

-- =========================================
-- FUNCTIONS
-- =========================================

CREATE OR REPLACE FUNCTION set_updated_at()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- =========================================
-- CATEGORY
-- =========================================

CREATE TABLE category (
                          id BIGSERIAL PRIMARY KEY,

                          name VARCHAR(100) NOT NULL UNIQUE,
                          slug VARCHAR(100) NOT NULL UNIQUE,

                          description TEXT,

                          created_at TIMESTAMPTZ DEFAULT NOW()
);

-- =========================================
-- SECTION TYPE
-- =========================================

CREATE TABLE section_type (
                              id SERIAL PRIMARY KEY,

                              name VARCHAR(100) NOT NULL UNIQUE,
                              description TEXT,

                              created_at TIMESTAMPTZ DEFAULT NOW()
);

-- =========================================
-- ROLE
-- =========================================

CREATE TABLE role (
                      id SERIAL PRIMARY KEY,

                      name VARCHAR(50) NOT NULL UNIQUE,
                      description TEXT,

                      created_at TIMESTAMPTZ DEFAULT NOW()
);

-- =========================================
-- USERS
-- =========================================

CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,

                       username VARCHAR(100) NOT NULL UNIQUE,
                       email VARCHAR(255) NOT NULL UNIQUE,

                       password_hash VARCHAR(255) NOT NULL,

                       full_name VARCHAR(255),
                       avatar_url TEXT,
                       phone_number VARCHAR(20),

                       status VARCHAR(50) NOT NULL DEFAULT 'PENDING',

                       provider VARCHAR(50),
                       provider_id VARCHAR(255),

                       is_verified BOOLEAN NOT NULL DEFAULT FALSE,

                       last_login_at TIMESTAMPTZ,

                       created_at TIMESTAMPTZ DEFAULT NOW(),
                       updated_at TIMESTAMPTZ DEFAULT NOW(),
                       deleted_at TIMESTAMPTZ,

                       version INTEGER NOT NULL DEFAULT 0
);

-- =========================================
-- USER ROLE
-- =========================================

CREATE TABLE user_role (
                           user_id BIGINT NOT NULL,
                           role_id INT NOT NULL,

                           PRIMARY KEY(user_id, role_id),

                           CONSTRAINT fk_user_role_user
                               FOREIGN KEY (user_id)
                                   REFERENCES users(id)
                                   ON DELETE CASCADE,

                           CONSTRAINT fk_user_role_role
                               FOREIGN KEY (role_id)
                                   REFERENCES role(id)
                                   ON DELETE CASCADE
);

-- =========================================
-- VERIFICATION TOKEN
-- =========================================

CREATE TABLE verification_token (
                                    id BIGSERIAL PRIMARY KEY,

                                    token VARCHAR(255) NOT NULL UNIQUE,

                                    user_id BIGINT NOT NULL,

                                    expiry_date TIMESTAMPTZ NOT NULL,

                                    used_at TIMESTAMPTZ,
                                    resent_at TIMESTAMPTZ,

                                    created_at TIMESTAMPTZ DEFAULT NOW(),
                                    updated_at TIMESTAMPTZ DEFAULT NOW(),

                                    CONSTRAINT fk_verification_token_user
                                        FOREIGN KEY (user_id)
                                            REFERENCES users(id)
                                            ON DELETE CASCADE
);

-- =========================================
-- USER SESSION
-- =========================================

CREATE TABLE user_session (
                              id BIGSERIAL PRIMARY KEY,

                              user_id BIGINT NOT NULL,

                              refresh_token TEXT NOT NULL UNIQUE,

                              expires_at TIMESTAMPTZ NOT NULL,

                              revoked_at TIMESTAMPTZ,

                              created_at TIMESTAMPTZ DEFAULT NOW(),
                              updated_at TIMESTAMPTZ DEFAULT NOW(),

                              CONSTRAINT fk_user_session_user
                                  FOREIGN KEY (user_id)
                                      REFERENCES users(id)
                                      ON DELETE CASCADE
);

-- =========================================
-- SYMPTOM
-- =========================================

CREATE TABLE symptom (
                         id BIGSERIAL PRIMARY KEY,

                         name VARCHAR(255) NOT NULL,
                         slug VARCHAR(255) NOT NULL UNIQUE,

                         description TEXT,

                         created_at TIMESTAMPTZ DEFAULT NOW(),
                         updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- =========================================
-- DISEASE
-- =========================================

CREATE TABLE disease (
                         id BIGSERIAL PRIMARY KEY,

                         name VARCHAR(255) NOT NULL UNIQUE,
                         slug VARCHAR(255) NOT NULL UNIQUE,

                         category_id BIGINT,

                         current_version_id BIGINT,

                         created_at TIMESTAMPTZ DEFAULT NOW(),
                         updated_at TIMESTAMPTZ DEFAULT NOW(),
                         deleted_at TIMESTAMPTZ,

                         version INTEGER NOT NULL DEFAULT 0,

                         CONSTRAINT fk_disease_category
                             FOREIGN KEY (category_id)
                                 REFERENCES category(id)
);

-- =========================================
-- DISEASE VERSION
-- =========================================

CREATE TABLE disease_version (
                                 id BIGSERIAL PRIMARY KEY,

                                 disease_id BIGINT NOT NULL,

                                 version_number INTEGER NOT NULL,

                                 created_by BIGINT NOT NULL,

                                 status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',

                                 moderation_note TEXT,

                                 reviewed_by BIGINT,
                                 reviewed_at TIMESTAMPTZ,

                                 deleted_at TIMESTAMPTZ,

                                 created_at TIMESTAMPTZ DEFAULT NOW(),
                                 updated_at TIMESTAMPTZ DEFAULT NOW(),

                                 version INTEGER NOT NULL DEFAULT 0,

                                 CONSTRAINT fk_disease_version_disease
                                     FOREIGN KEY (disease_id)
                                         REFERENCES disease(id)
                                         ON DELETE CASCADE,

                                 CONSTRAINT fk_disease_version_created_by
                                     FOREIGN KEY (created_by)
                                         REFERENCES users(id),

                                 CONSTRAINT fk_disease_version_reviewed_by
                                     FOREIGN KEY (reviewed_by)
                                         REFERENCES users(id),

                                 CONSTRAINT uk_disease_version
                                     UNIQUE(disease_id, version_number)
);

-- =========================================
-- CURRENT VERSION FK
-- =========================================

ALTER TABLE disease
    ADD CONSTRAINT fk_disease_current_version
        FOREIGN KEY (current_version_id)
            REFERENCES disease_version(id);

-- =========================================
-- DISEASE SECTION
-- =========================================

CREATE TABLE disease_section (
                                 id BIGSERIAL PRIMARY KEY,

                                 disease_version_id BIGINT NOT NULL,
                                 section_type_id INT NOT NULL,

                                 title VARCHAR(255) NOT NULL,
                                 content TEXT,

                                 order_index INTEGER NOT NULL DEFAULT 0,

                                 created_at TIMESTAMPTZ DEFAULT NOW(),
                                 updated_at TIMESTAMPTZ DEFAULT NOW(),
                                 deleted_at TIMESTAMPTZ,

                                 CONSTRAINT fk_disease_section_version
                                     FOREIGN KEY (disease_version_id)
                                         REFERENCES disease_version(id)
                                         ON DELETE CASCADE,

                                 CONSTRAINT fk_disease_section_type
                                     FOREIGN KEY (section_type_id)
                                         REFERENCES section_type(id)
);

-- =========================================
-- DISEASE VERSION SYMPTOM
-- =========================================

CREATE TABLE disease_version_symptom (
                                         id BIGSERIAL PRIMARY KEY,

                                         disease_version_id BIGINT NOT NULL,
                                         symptom_id BIGINT NOT NULL,

                                         weight_score DECIMAL(5,2) NOT NULL DEFAULT 1.0,

                                         created_at TIMESTAMPTZ DEFAULT NOW(),

                                         CONSTRAINT fk_dvs_version
                                             FOREIGN KEY (disease_version_id)
                                                 REFERENCES disease_version(id)
                                                 ON DELETE CASCADE,

                                         CONSTRAINT fk_dvs_symptom
                                             FOREIGN KEY (symptom_id)
                                                 REFERENCES symptom(id),

                                         CONSTRAINT uk_dvs
                                             UNIQUE(disease_version_id, symptom_id)
);

-- =========================================
-- CASE STUDY
-- =========================================

CREATE TABLE case_study (
                            id BIGSERIAL PRIMARY KEY,

                            title VARCHAR(255) NOT NULL,

                            slug VARCHAR(255) NOT NULL UNIQUE,

                            description TEXT,

                            difficulty VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',

                            diagnosis TEXT,

                            learning_notes TEXT,

                            patient_age INTEGER,

                            patient_gender VARCHAR(20),

                            chief_complaint TEXT,

                            case_question TEXT,

                            explanation TEXT,

                            view_count BIGINT NOT NULL DEFAULT 0,

                            is_featured BOOLEAN NOT NULL DEFAULT FALSE,

                            created_by BIGINT NOT NULL,

                            status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',

                            created_at TIMESTAMPTZ DEFAULT NOW(),
                            updated_at TIMESTAMPTZ DEFAULT NOW(),
                            deleted_at TIMESTAMPTZ,

                            CONSTRAINT fk_case_study_user
                                FOREIGN KEY (created_by)
                                    REFERENCES users(id)
);

-- =========================================
-- CASE STUDY SYMPTOM
-- =========================================

CREATE TABLE case_study_symptom (
                                    case_study_id BIGINT NOT NULL,
                                    symptom_id BIGINT NOT NULL,

                                    PRIMARY KEY(case_study_id, symptom_id),

                                    CONSTRAINT fk_css_case
                                        FOREIGN KEY (case_study_id)
                                            REFERENCES case_study(id)
                                            ON DELETE CASCADE,

                                    CONSTRAINT fk_css_symptom
                                        FOREIGN KEY (symptom_id)
                                            REFERENCES symptom(id)
);

-- =========================================
-- AUDIT LOG
-- =========================================

CREATE TABLE audit_log (
                           id BIGSERIAL PRIMARY KEY,

                           user_id BIGINT,

                           action_type VARCHAR(50) NOT NULL,

                           entity_name VARCHAR(100) NOT NULL,
                           entity_id BIGINT NOT NULL,

                           old_data JSONB,
                           new_data JSONB,

                           reason TEXT,

                           ip_address INET,
                           user_agent TEXT,

                           created_at TIMESTAMPTZ DEFAULT NOW(),

                           CONSTRAINT fk_audit_user
                               FOREIGN KEY (user_id)
                                   REFERENCES users(id)
);

-- =========================================
-- TRIGGERS
-- =========================================

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_symptom_updated_at
    BEFORE UPDATE ON symptom
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_disease_updated_at
    BEFORE UPDATE ON disease
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_disease_version_updated_at
    BEFORE UPDATE ON disease_version
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_disease_section_updated_at
    BEFORE UPDATE ON disease_section
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_case_study_updated_at
    BEFORE UPDATE ON case_study
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_verification_token_updated_at
    BEFORE UPDATE ON verification_token
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_user_session_updated_at
    BEFORE UPDATE ON user_session
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- =========================================
-- INDEXES
-- =========================================

CREATE INDEX idx_users_email
    ON users(email);

CREATE INDEX idx_users_username
    ON users(username);

CREATE INDEX idx_disease_slug
    ON disease(slug);

CREATE INDEX idx_symptom_slug
    ON symptom(slug);

CREATE INDEX idx_case_study_slug
    ON case_study(slug);

CREATE INDEX idx_disease_version_status
    ON disease_version(status);

CREATE INDEX idx_audit_entity
    ON audit_log(entity_name, entity_id);

-- =========================================
-- SEED DATA
-- =========================================

INSERT INTO role(name, description)
VALUES
    ('USER', 'Medical learner and Content contributor'),
    ('REVIEWER', 'Content reviewer'),
    ('ADMIN', 'System administrator');

INSERT INTO section_type(name, description)
VALUES
    ('definition', 'Disease definition'),
    ('symptoms', 'Clinical symptoms'),
    ('causes', 'Disease causes'),
    ('diagnosis', 'Diagnosis methods'),
    ('treatment', 'Treatment methods'),
    ('prevention', 'Prevention methods');
