BEGIN;

CREATE TABLE users (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    role VARCHAR(30) NOT NULL CHECK (role IN ('STUDENT', 'ADMIN')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE students (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id INTEGER NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    university VARCHAR(200),
    degree VARCHAR(150),
    graduation_year INTEGER CHECK (graduation_year BETWEEN 1900 AND 2200),
    bio VARCHAR(1000)
);

CREATE TABLE skills (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(100),
    description VARCHAR(500)
);

CREATE TABLE student_skills (
    student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    skill_id INTEGER NOT NULL REFERENCES skills(id) ON DELETE RESTRICT,
    proficiency_level SMALLINT NOT NULL CHECK (proficiency_level BETWEEN 1 AND 5),
    PRIMARY KEY (student_id, skill_id)
);

CREATE TABLE certifications (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    issuing_organization VARCHAR(200) NOT NULL,
    issue_date DATE,
    expiry_date DATE,
    credential_url VARCHAR(500),
    CONSTRAINT valid_certification_dates CHECK (expiry_date IS NULL OR issue_date IS NULL OR expiry_date >= issue_date)
);

CREATE TABLE projects (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    owner_student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    repository_url VARCHAR(500),
    start_date DATE,
    end_date DATE,
    CONSTRAINT valid_project_dates CHECK (end_date IS NULL OR start_date IS NULL OR end_date >= start_date)
);

CREATE TABLE companies (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(200) NOT NULL UNIQUE,
    industry VARCHAR(150),
    website VARCHAR(500),
    minimum_gpa NUMERIC(3, 2) CHECK (minimum_gpa BETWEEN 0 AND 10)
);

CREATE TABLE company_requirements (
    company_id INTEGER NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    skill_id INTEGER NOT NULL REFERENCES skills(id) ON DELETE RESTRICT,
    minimum_proficiency SMALLINT NOT NULL CHECK (minimum_proficiency BETWEEN 1 AND 5),
    required BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (company_id, skill_id)
);

CREATE TABLE teams (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(1000),
    created_by INTEGER NOT NULL REFERENCES students(id) ON DELETE RESTRICT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE team_members (
    team_id INTEGER NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    member_role VARCHAR(100),
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (team_id, student_id)
);

CREATE TABLE recommendations (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    recommendation_type VARCHAR(30) NOT NULL CHECK (recommendation_type IN ('SKILL', 'COMPANY', 'TEAMMATE')),
    target_id INTEGER NOT NULL CHECK (target_id > 0),
    score NUMERIC(5, 4) CHECK (score BETWEEN 0 AND 1),
    reason VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_student_skills_skill_id ON student_skills(skill_id);
CREATE INDEX idx_certifications_student_id ON certifications(student_id);
CREATE INDEX idx_projects_owner_student_id ON projects(owner_student_id);
CREATE INDEX idx_company_requirements_skill_id ON company_requirements(skill_id);
CREATE INDEX idx_team_members_student_id ON team_members(student_id);
CREATE INDEX idx_recommendations_student_id ON recommendations(student_id);

COMMIT;
