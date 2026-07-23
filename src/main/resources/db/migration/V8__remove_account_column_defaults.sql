-- V7 uses temporary defaults so the same migration can backfill existing rows
-- on both MySQL and H2. New accounts must always provide explicit credentials.
ALTER TABLE player ALTER COLUMN username DROP DEFAULT;
ALTER TABLE player ALTER COLUMN password_hash DROP DEFAULT;
