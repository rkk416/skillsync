# SkillSync – Career & Collaboration Intelligence Platform

SkillSync is a JavaFX desktop application foundation for student profiles, placement readiness, team collaboration, and career recommendations. This repository intentionally contains architecture and contracts only; business rules, algorithms, persistence operations, and feature user interfaces belong in future feature branches.

## Technology

- Java 21
- JavaFX 21
- PostgreSQL with the PostgreSQL JDBC driver
- HikariCP connection pooling
- Maven
- MVC and feature-oriented packages

## Prerequisites

- JDK 21
- Maven 3.9+
- PostgreSQL 15+
- Git

## Setup

1. Clone the repository and switch to `develop`.
2. Copy `src/main/resources/application.properties.example` to `src/main/resources/application.properties`.
3. Set `DB_URL`, `DB_USER`, and `DB_PASSWORD`. For example, use `jdbc:postgresql://localhost:5432/skillsync` as the URL.
4. Create the database and apply `src/main/resources/schema.sql` with PostgreSQL tooling.
5. Verify the project with `mvn clean verify`.
6. Launch the JavaFX shell with `mvn javafx:run`.

The real `application.properties` file is ignored by Git. Never commit credentials.

## Architecture

```text
src/main/java/skillsync/
├── auth/              authentication MVC components
├── profile/           profile MVC components
├── placement/         placement MVC components
├── collaboration/     collaboration MVC components
├── recommendation/    recommendation MVC components
├── dashboard/         dashboard and analytics MVC components
├── model/             data-only domain models
├── service/           feature service contracts
├── database/          connection-pool infrastructure
├── utils/             shared stateless utilities
└── main/              JavaFX application lifecycle
```

Controllers should coordinate views and service contracts. Views should contain presentation concerns only. Service implementations should own use-case orchestration, while repositories/DAOs added later should isolate SQL and persistence concerns. Depend on interfaces at feature boundaries.

## Current Scope

Included: domain models, service interfaces, JavaFX entry point, pooled database connection foundation, relational schema, and team workflow documentation.

Excluded: business logic, GUI workflows, database CRUD implementations, recommendation algorithms, and placement-analysis logic.

## Build Commands

```bash
mvn clean verify
mvn javafx:run
```

See [TEAM_GUIDELINES.md](TEAM_GUIDELINES.md) before contributing.
