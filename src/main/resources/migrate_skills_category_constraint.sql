-- Migration: clean skills.category and add CHECK constraint
-- Scope: skills table only. No data loss.
--

BEGIN;

-- 1. Update any invalid or blank category values to 'Other'
UPDATE skills
SET category = 'Other'
WHERE category IS NULL
   OR category = ''
   OR category NOT IN ('Programming', 'Database', 'Frontend', 'Backend', 'Cloud', 'AI / ML', 'Cybersecurity', 'DevOps', 'Mobile Development', 'Data Science', 'Other');

-- 2. Add CHECK constraint on category column
-- Note: We drop it first if it exists (using a custom name for the constraint if desired, or we just drop/create the check)
ALTER TABLE skills DROP CONSTRAINT IF EXISTS skills_category_check;
ALTER TABLE skills ADD CONSTRAINT skills_category_check CHECK (category IN ('Programming', 'Database', 'Frontend', 'Backend', 'Cloud', 'AI / ML', 'Cybersecurity', 'DevOps', 'Mobile Development', 'Data Science', 'Other'));

COMMIT;
