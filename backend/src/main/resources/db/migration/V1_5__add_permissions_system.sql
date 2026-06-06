-- =========================================
-- PERMISSIONS SYSTEM
-- Add dynamic permissions to fixed roles
-- =========================================

-- Permission table
CREATE TABLE permission (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_permission_name ON permission(name);
CREATE INDEX idx_permission_category ON permission(category);

-- Role-Permission mapping
CREATE TABLE role_permission (
    role_id INTEGER NOT NULL REFERENCES role(id) ON DELETE CASCADE,
    permission_id INTEGER NOT NULL REFERENCES permission(id) ON DELETE CASCADE,
    granted_at TIMESTAMPTZ DEFAULT NOW(),
    PRIMARY KEY (role_id, permission_id)
);

CREATE INDEX idx_role_permission_role ON role_permission(role_id);
CREATE INDEX idx_role_permission_permission ON role_permission(permission_id);

-- =========================================
-- SEED PERMISSIONS (14 total)
-- =========================================

INSERT INTO permission(name, description, category) VALUES
-- Disease domain (4)
('DISEASE_WRITE', 'Create and update disease metadata', 'DISEASE'),
('DISEASE_DELETE', 'Soft delete diseases', 'DISEASE'),
('DISEASE_RESTORE', 'Restore soft-deleted diseases', 'DISEASE'),
('DISEASE_MANAGE', 'Manage disease categories', 'DISEASE'),

-- Version domain (4)
('VERSION_WRITE', 'Create, clone, update, and delete own versions', 'VERSION'),
('VERSION_SUBMIT', 'Submit versions for review', 'VERSION'),
('VERSION_REVIEW', 'Approve or reject versions', 'VERSION'),
('VERSION_ARCHIVE', 'Archive old versions', 'VERSION'),

-- Section domain (2)
('SECTION_WRITE', 'Create, update, delete sections in own versions', 'SECTION'),
('SECTION_EDIT_ANY', 'Edit sections in any version (bypass ownership)', 'SECTION'),

-- User domain (3)
('USER_VIEW_ALL', 'View all user accounts', 'USER'),
('USER_MANAGE', 'Activate and deactivate users', 'USER'),
('ROLE_ASSIGN', 'Assign and remove roles', 'USER'),

-- System domain (1)
('AUDIT_VIEW', 'View audit logs', 'SYSTEM');

-- =========================================
-- MAP PERMISSIONS TO ROLES
-- =========================================

-- USER role (4 permissions)
INSERT INTO role_permission(role_id, permission_id)
SELECT r.id, p.id
FROM role r, permission p
WHERE r.name = 'USER'
AND p.name IN (
    'DISEASE_WRITE',
    'VERSION_WRITE',
    'VERSION_SUBMIT',
    'SECTION_WRITE'
);

-- REVIEWER role (10 permissions = USER + 6)
INSERT INTO role_permission(role_id, permission_id)
SELECT r.id, p.id
FROM role r, permission p
WHERE r.name = 'REVIEWER'
AND p.name IN (
    -- USER permissions
    'DISEASE_WRITE',
    'VERSION_WRITE',
    'VERSION_SUBMIT',
    'SECTION_WRITE',
    -- REVIEWER-specific permissions
    'VERSION_REVIEW',
    'VERSION_ARCHIVE',
    'DISEASE_DELETE',
    'DISEASE_RESTORE',
    'DISEASE_MANAGE',
    'SECTION_EDIT_ANY'
);

-- ADMIN role (14 permissions = REVIEWER + 4)
INSERT INTO role_permission(role_id, permission_id)
SELECT r.id, p.id
FROM role r, permission p
WHERE r.name = 'ADMIN'
AND p.name IN (
    -- REVIEWER permissions
    'DISEASE_WRITE',
    'VERSION_WRITE',
    'VERSION_SUBMIT',
    'SECTION_WRITE',
    'VERSION_REVIEW',
    'VERSION_ARCHIVE',
    'DISEASE_DELETE',
    'DISEASE_RESTORE',
    'DISEASE_MANAGE',
    'SECTION_EDIT_ANY',
    -- ADMIN-specific permissions
    'USER_VIEW_ALL',
    'USER_MANAGE',
    'ROLE_ASSIGN',
    'AUDIT_VIEW'
);
