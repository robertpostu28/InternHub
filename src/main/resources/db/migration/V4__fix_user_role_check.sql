-- Fix users.role CHECK constraint to match the Java Role enum values

ALTER TABLE users
    DROP CONSTRAINT check_user_role;

ALTER TABLE users
    ADD CONSTRAINT check_user_role
        CHECK (role IN ('CANDIDATE', 'RECRUITER'));