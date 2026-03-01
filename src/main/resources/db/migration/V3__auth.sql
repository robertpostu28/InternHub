-- authentification hardening

-- add an "enabled" flag to users to allow disabling accounts without deleting them
ALTER TABLE users
    ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT TRUE;

-- restrict allowed roles to a predefined set
ALTER TABLE users
    ADD CONSTRAINT check_user_role CHECK (role IN ('admin', 'user', 'guest'));