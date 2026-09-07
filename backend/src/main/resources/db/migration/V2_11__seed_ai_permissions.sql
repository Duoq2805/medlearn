-- =========================================
-- SEED AI PERMISSIONS
-- Add AI domain permissions and map to ADMIN
-- =========================================

-- Insert AI permissions into permission table
INSERT INTO permission (name, description, category)
VALUES
    ('AI_USE', 'Use AI features and prompt builder', 'AI'),
    ('AI_SUMMARY', 'Generate and view AI summaries', 'AI'),
    ('AI_MANAGE', 'Manage AI prompt templates and system configurations', 'AI'),
    ('AI_VIEW_USAGE', 'View AI token usage logs and statistics', 'AI')
ON CONFLICT (name) DO NOTHING;

-- Map AI permissions to ADMIN role
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM role r, permission p
WHERE r.name = 'ADMIN'
AND p.name IN ('AI_USE', 'AI_SUMMARY', 'AI_MANAGE', 'AI_VIEW_USAGE')
ON CONFLICT (role_id, permission_id) DO NOTHING;
