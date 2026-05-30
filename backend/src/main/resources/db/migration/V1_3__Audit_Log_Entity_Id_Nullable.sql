-- Make entity_id nullable for anonymous security events (LOGIN_BLOCKED, etc.)
ALTER TABLE audit_log ALTER COLUMN entity_id DROP NOT NULL;
