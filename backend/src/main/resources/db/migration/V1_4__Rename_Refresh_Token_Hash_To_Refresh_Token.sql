-- Rename refresh_token_hash back to refresh_token for direct storage
-- Refresh tokens are already cryptographically secure random values (256-bit)
-- BCrypt hashing is unnecessary and causes lookup failures (non-deterministic)

-- Clear all existing sessions (BCrypt hashes are incompatible with direct storage)
-- Users will need to log in again after this migration
DELETE FROM user_session;

-- Rename column back to refresh_token
ALTER TABLE user_session
RENAME COLUMN refresh_token_hash TO refresh_token;

-- Change column type to TEXT to accommodate full token length
ALTER TABLE user_session
ALTER COLUMN refresh_token TYPE TEXT;

-- Ensure NOT NULL and UNIQUE constraints
ALTER TABLE user_session
ALTER COLUMN refresh_token SET NOT NULL;

-- Unique constraint should already exist, but ensure it's on correct column
-- (The constraint was likely renamed automatically, but verify)
