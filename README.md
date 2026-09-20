# TaskFlow

TaskFlow is a Spring Boot REST API and browser-based task dashboard for managing personal work. The backend is the source of truth for authentication, task ownership, filtering, and pagination. The frontend is a lightweight HTML, CSS, and vanilla JavaScript client served by Spring Boot.

## Features

- User registration and JWT login
- Create, view, update, and delete tasks
- Task status, priority, description, and due date
- Server-side status, priority, and text search filters
- Server-side pagination
- Per-user task ownership enforcement
- Swagger/OpenAPI documentation

## Architecture

The backend follows a layered structure: controllers accept HTTP requests and DTOs, services contain business logic, repositories handle persistence, and JPA/Hibernate maps entities to the database. Spring Security and a JWT filter protect task endpoints.

## Technology Stack

- Java 21
- Spring Boot 4.1.1
- Spring MVC, Spring Security, Spring Data JPA, Bean Validation
- MySQL at runtime; H2 is used by tests
- JJWT 0.13.0
- springdoc OpenAPI 3.1.1
- Plain HTML, CSS, and JavaScript frontend

## Authentication and Security

`POST /api/auth/login` returns a JWT and the authenticated user's basic information. The frontend keeps that token in `sessionStorage` for the current browser tab and sends it as `Authorization: Bearer <token>` for task requests. Every task query is scoped to the authenticated user; users cannot access another user's tasks.

## API Endpoints

### Authentication

`POST /api/auth/register`

```json
{
  "name": "Ada Lovelace",
  "email": "ada@example.com",
  "password": "correct-horse-battery"
}
```

Returns `201 Created` with `id`, `name`, `email`, and `createdAt`.

`POST /api/auth/login`

```json
{
  "email": "ada@example.com",
  "password": "correct-horse-battery"
}
```

Returns a response shaped like:

```json
{
  "token": "<jwt>",
  "tokenType": "Bearer",
  "user": { "id": 1, "name": "Ada Lovelace", "email": "ada@example.com" }
}
```

### Tasks

All task endpoints require a Bearer JWT.

- `POST /api/tasks` creates a task.
- `GET /api/tasks?page=0&size=10&status=TODO&priority=HIGH&search=report` lists the authenticated user's tasks. `status`, `priority`, and `search` are optional; `size` is limited to 1-100.
- `GET /api/tasks/{taskId}` gets one owned task.
- `PUT /api/tasks/{taskId}` updates one owned task.
- `DELETE /api/tasks/{taskId}` deletes one owned task and returns `204 No Content`.

Create and update requests use this shape:

```json
{
  "title": "Review release notes",
  "description": "Check the final changes before publishing.",
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2026-10-01"
}
```

Task list responses contain `content`, `page`, `size`, `totalElements`, and `totalPages`. Each task has `id`, `title`, `description`, `status`, `priority`, `dueDate`, `createdAt`, and `updatedAt`.

Validation failures return `400`; invalid credentials return `401`; ownership violations return `403`; missing resources return `404`; duplicate email or other data conflicts return `409`.

## Database Configuration

The runtime database is MySQL. Schema validation is enabled by default, so the database schema must already exist and match the entities. Tests use the test configuration and H2.

Required environment variables:

- `TASKFLOW_DB_URL`
- `TASKFLOW_DB_USERNAME`
- `TASKFLOW_DB_PASSWORD`
- `TASKFLOW_JWT_SECRET`

Optional:

- `TASKFLOW_JPA_DDL_AUTO` (defaults to `validate`)
- `TASKFLOW_JWT_EXPIRATION` (defaults to `86400000` milliseconds)

Do not put real credentials or secrets in source control.

## Local Setup

1. Install Java 21 and MySQL.
2. Create a MySQL database and provide the environment variables above in your shell.
3. From `backend`, run `./mvnw test` on macOS/Linux or `./mvnw.cmd test` on Windows.
4. Start the application with `./mvnw spring-boot:run` or `./mvnw.cmd spring-boot:run`.
5. Open `http://localhost:8080/` to use the dashboard.

## Tests and Packaging

From `backend`:

```text
./mvnw test
./mvnw package
```

On Windows, use `./mvnw.cmd` instead of `./mvnw`. The packaged Spring Boot JAR is written to `backend/target/`.

## Swagger/OpenAPI

When the application is running, use the interactive documentation at `http://localhost:8080/swagger-ui.html`. The generated OpenAPI document is available at `http://localhost:8080/v3/api-docs`.

## Frontend Usage

Open the root application URL, create an account or sign in, then manage tasks from the dashboard. Filters and search are sent to the existing task list endpoint, and the session is cleared by the Log out action.

## Project Structure

```text
TaskFlow/
├── README.md
└── backend/
    ├── pom.xml
    └── src/
        ├── main/java/com/taskflow/
        │   ├── config/ controller/ dto/ entity/ exception/
        │   ├── repository/ security/ service/
        │   └── TaskflowApplication.java
        ├── main/resources/
        │   ├── application.properties
        │   └── static/ (frontend)
        └── test/
```

## Screenshots

_Screenshots can be added here as the UI evolves._