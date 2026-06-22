# SkillSync Team Guidelines

## Branch Strategy

`main` contains approved, stable releases. `develop` is the integration branch. Work is performed in these feature branches:

- `feature/auth`
- `feature/profile`
- `feature/placement`
- `feature/collaboration`
- `feature/recommendation`

Create short-lived sub-branches from the relevant feature branch when concurrent work requires them. Merge feature branches into `develop` by pull request; promote `develop` to `main` by a release pull request.

## Required Rules

- Never push directly to `main`.
- Commit meaningful progress daily while actively working.
- Every merge requires a pull request and at least one teammate's approval.
- Do not change `schema.sql` without team-lead or database-owner approval.
- Keep credentials, local configuration, generated files, and IDE metadata out of Git.
- Do not merge code that fails `mvn clean verify`.

## Pull Requests

Keep pull requests focused on one feature or concern. Include a concise summary, testing evidence, schema impact, and screenshots when a user interface is eventually introduced. Resolve review comments before merging and use a clear squash-merge message.

## Commit Style

Use imperative, scoped messages such as `feat(profile): add student repository contract` or `docs(team): clarify review policy`. Prefer small commits that compile independently.

## Code Standards

- Follow Java naming conventions and keep packages lowercase.
- Apply Clean Code and SOLID principles; favor small, cohesive classes.
- Keep models free of business logic.
- Program against service interfaces and use dependency injection at boundaries.
- Keep JavaFX controllers thin and never place SQL in controllers.
- Document public APIs when intent is not obvious.
- Add tests alongside every implemented behavior.

## Ownership for a Five-Person Team

Suggested primary ownership: authentication, profiles, placement, collaboration, and recommendation. Ownership means coordinating reviews—not exclusive control. At least one person outside the owning area should review cross-cutting changes.
