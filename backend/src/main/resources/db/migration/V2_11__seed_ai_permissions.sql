-- Seed AI permissions into role_permission for ADMIN role
-- Assumes ADMIN role exists with id = 1
INSERT INTO role_permission (role_id, permission_code)
SELECT 1, p
FROM (VALUES
    ('AI_USE'),
    ('AI_SUMMARY'),
    ('AI_FLASHCARD'),
    ('AI_QUIZ'),
    ('AI_CASE'),
    ('AI_CHAT'),
    ('AI_REPORT'),
    ('AI_MANAGE'),
    ('AI_VIEW_USAGE'),
    ('AI_ADMIN')
) AS perms(p)
WHERE NOT EXISTS (
    SELECT 1 FROM role_permission rp
    WHERE rp.role_id = 1 AND rp.permission_code = perms.p
);
