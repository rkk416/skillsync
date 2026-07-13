-- Migration: add columns required by Profile Developer Handbook v1, Section 5 & 9
-- Scope: students table only (module-owned table). No renames, no drops, no data loss.
--
-- Fixes over v1:
--   1. Uses IF NOT EXISTS so this is safe to run whether or not the DB already has these
--      columns (e.g. if it was created fresh from schema.sql, which already defines them).
--   2. Widens cgpa to NUMERIC(4,2). NUMERIC(3,2) can only store up to 9.99, but the
--      handbook's valid range is 0.0–10.0 inclusive — entering exactly 10.0 previously
--      threw "numeric field overflow" at save time. This ALTER runs unconditionally so it
--      also repairs databases built from schema.sql's original NUMERIC(3,2) definition.

BEGIN;

-- Handbook Sec 5 & 6: Personal Information tab requires a "Branch" field (e.g. CSE, ECE, IT).
ALTER TABLE students
    ADD COLUMN IF NOT EXISTS branch VARCHAR(50);

-- Handbook Sec 5, 6, 9, 11: Personal Information tab requires "CGPA" with validation 0.0-10.0.
ALTER TABLE students
    ADD COLUMN IF NOT EXISTS cgpa NUMERIC(4, 2) CHECK (cgpa BETWEEN 0 AND 10);

-- Guarantee correct precision regardless of whether cgpa was just added above or already
-- existed (e.g. from schema.sql's NUMERIC(3,2)).
ALTER TABLE students
    ALTER COLUMN cgpa TYPE NUMERIC(4, 2);

COMMIT;