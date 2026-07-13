BEGIN;

ALTER TABLE students ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE students ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE skills ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE skills ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE certifications ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE certifications ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE projects ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE projects ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE companies ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE companies ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE teams ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE recommendations ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE projects
ADD COLUMN IF NOT EXISTS technology_stack VARCHAR(500);
-- TODO: Keep recommendations.target_id generic for compatibility; evaluate typed target references only in a dedicated migration.
CREATE TABLE IF NOT EXISTS placement_applications (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    company_id INTEGER NOT NULL REFERENCES companies(id) ON DELETE RESTRICT,
    status VARCHAR(30) NOT NULL DEFAULT 'APPLIED' CHECK (status IN ('APPLIED', 'SHORTLISTED', 'REJECTED', 'SELECTED', 'WITHDRAWN')),
    placement_score NUMERIC(5, 2) CHECK (placement_score BETWEEN 0 AND 100),
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_placement_applications_student_company UNIQUE (student_id, company_id)
);

CREATE TABLE IF NOT EXISTS team_join_requests (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    team_id INTEGER NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')),
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS student_connections (
    student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    connected_student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACCEPTED', 'BLOCKED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (student_id, connected_student_id),
    CONSTRAINT valid_student_connection_pair CHECK (student_id <> connected_student_id)
);

CREATE TABLE IF NOT EXISTS login_history (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    login_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    logout_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS activity_logs (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    activity_type VARCHAR(50) NOT NULL,
    description VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS recommendation_history (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    recommendation_type VARCHAR(30) NOT NULL,
    target_id INTEGER NOT NULL CHECK (target_id > 0),
    score NUMERIC(5, 4) CHECK (score BETWEEN 0 AND 1),
    algorithm_version VARCHAR(50),
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_placement_applications_student_id ON placement_applications(student_id);
CREATE INDEX IF NOT EXISTS idx_placement_applications_company_id ON placement_applications(company_id);
CREATE INDEX IF NOT EXISTS idx_team_join_requests_team_id ON team_join_requests(team_id);
CREATE INDEX IF NOT EXISTS idx_team_join_requests_student_id ON team_join_requests(student_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_team_join_requests_pending ON team_join_requests(team_id, student_id) WHERE status = 'PENDING';
CREATE INDEX IF NOT EXISTS idx_student_connections_connected_student_id ON student_connections(connected_student_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_student_connections_pair ON student_connections(LEAST(student_id, connected_student_id), GREATEST(student_id, connected_student_id));
CREATE INDEX IF NOT EXISTS idx_login_history_user_id ON login_history(user_id);
CREATE INDEX IF NOT EXISTS idx_activity_logs_student_id ON activity_logs(student_id);
CREATE INDEX IF NOT EXISTS idx_recommendation_history_student_id ON recommendation_history(student_id);

COMMIT;

BEGIN;
ALTER TABLE students
    ADD COLUMN IF NOT EXISTS branch VARCHAR(50);
ALTER TABLE students
    ADD COLUMN IF NOT EXISTS cgpa NUMERIC(4, 2) CHECK (cgpa BETWEEN 0 AND 10);
ALTER TABLE students
    ALTER COLUMN cgpa TYPE NUMERIC(4, 2);

COMMIT;