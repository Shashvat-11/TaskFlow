# TaskFlow — AI Development Instructions

## Project

TaskFlow is a backend-focused task management REST API built with Java 21 and Spring Boot.

## Architecture

Use layered architecture:

Controller → DTO → Service → Repository → JPA/Hibernate → MySQL

## Rules

- Use Java 21.
- Follow Spring Boot conventions.
- Keep controllers thin.
- Business logic belongs in services.
- Database access belongs in repositories.
- Do not expose JPA entities directly through REST APIs.
- Use DTOs for API requests and responses.
- Validate incoming requests.
- Use centralized exception handling.
- Follow RESTful API conventions.
- Never hardcode passwords, database credentials, API keys, or JWT secrets.
- Use environment variables for secrets.
- Users must only access their own tasks.
- Prefer small, focused changes.
- Do not modify unrelated files.
- Do not introduce unnecessary dependencies.
- Do not rewrite working code unnecessarily.
- Run appropriate tests after meaningful changes.
- Fix compilation and test failures before considering a task complete.

## Development behavior

Before implementing a significant feature:

1. Inspect the existing project.
2. Understand the current architecture.
3. Identify files that need modification.
4. Implement the smallest coherent change.
5. Run tests/build verification.
6. Fix errors.
7. Summarize what changed.

Do not implement features that were not requested.

## Security

Authentication will use Spring Security and JWT.

Never commit real secrets.

## Frontend

The frontend is secondary to the backend. Keep it simple and functional.

## Documentation

Only document features that actually exist in the implementation.
