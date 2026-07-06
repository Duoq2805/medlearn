-- AI columns for users table
ALTER TABLE users
    ADD COLUMN monthly_token_quota   BIGINT      DEFAULT 1000000,
    ADD COLUMN tokens_used_this_month BIGINT     DEFAULT 0,
    ADD COLUMN quota_reset_at        TIMESTAMPTZ,
    ADD COLUMN ai_enabled            BOOLEAN     DEFAULT TRUE;
