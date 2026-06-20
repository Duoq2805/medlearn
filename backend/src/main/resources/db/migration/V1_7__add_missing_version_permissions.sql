-- =========================================
-- ADD MISSING VERSION PERMISSIONS
-- Ensures Flyway checksums remain valid after V1_5
-- =========================================

-- Insert missing permissions (idempotent due to ON CONFLICT or NOT EXISTS check)
INSERT INTO permission(name, description, category) 
SELECT 'VERSION_READ', 'Read draft versions', 'VERSION'
WHERE NOT EXISTS (SELECT 1 FROM permission WHERE name = 'VERSION_READ');

INSERT INTO permission(name, description, category) 
SELECT 'VERSION_DELETE', 'Soft delete versions', 'VERSION'
WHERE NOT EXISTS (SELECT 1 FROM permission WHERE name = 'VERSION_DELETE');

INSERT INTO permission(name, description, category) 
SELECT 'VERSION_RESTORE', 'Restore soft-deleted versions', 'VERSION'
WHERE NOT EXISTS (SELECT 1 FROM permission WHERE name = 'VERSION_RESTORE');

-- Map to USER role
INSERT INTO role_permission(role_id, permission_id)
SELECT r.id, p.id
FROM role r, permission p
WHERE r.name = 'USER'
AND p.name IN ('VERSION_READ', 'VERSION_DELETE', 'VERSION_RESTORE')
AND NOT EXISTS (
    SELECT 1 FROM role_permission rp 
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- Map to REVIEWER role
INSERT INTO role_permission(role_id, permission_id)
SELECT r.id, p.id
FROM role r, permission p
WHERE r.name = 'REVIEWER'
AND p.name IN ('VERSION_READ', 'VERSION_DELETE', 'VERSION_RESTORE')
AND NOT EXISTS (
    SELECT 1 FROM role_permission rp 
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- Map to ADMIN role
INSERT INTO role_permission(role_id, permission_id)
SELECT r.id, p.id
FROM role r, permission p
WHERE r.name = 'ADMIN'
AND p.name IN ('VERSION_READ', 'VERSION_DELETE', 'VERSION_RESTORE')
AND NOT EXISTS (
    SELECT 1 FROM role_permission rp 
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- Update description of VERSION_WRITE to match what was intended
UPDATE permission 
SET description = 'Create, clone, update own versions' 
WHERE name = 'VERSION_WRITE';
