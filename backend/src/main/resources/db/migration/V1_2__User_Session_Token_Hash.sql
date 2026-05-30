-- User session table update for refresh token hash
ALTER TABLE user_session
RENAME COLUMN refresh_token TO refresh_token_hash;

ALTER TABLE user_session
ALTER COLUMN refresh_token_hash SET NOT NULL;

ALTER TABLE user_session
ALTER COLUMN refresh_token_hash TYPE VARCHAR(255);

-- Optional: Add a unique constraint on refresh_token_hash if it's not already there and if needed
-- ALTER TABLE user_session ADD CONSTRAINT uq_refresh_token_hash UNIQUE (refresh_token_hash);
