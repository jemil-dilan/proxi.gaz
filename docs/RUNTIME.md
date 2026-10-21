# ProxiGaz — Runtime & Environment

## Chosen runtime

| Choice | Value |
|--------|--------|
| Language | Java 25 (Temurin) |
| Framework | Spring Boot 4.1.x |
| Build | Gradle (Kotlin DSL) via `./gradlew` |
| Persistence | PostgreSQL + Liquibase (+ PostGIS in Sprint 0 / PXG-003) |
| API contract | OpenAPI-first (`specs/openapi/main.yaml`) |

**Why not NestJS/Node:** PXG-001 already validated a hexagonal (clean) architecture on Spring Boot with contract-first OpenAPI, JPA, Liquibase, and Cucumber e2e. Changing stacks would discard that proven scaffold without benefit for the MVP backend plan.

## How to run

Prerequisites: JDK 25, Docker (for Testcontainers / local Postgres), a reachable PostgreSQL instance for local `bootRun`.

```bash
# Local app (uses env vars / defaults from application.yaml)
./gradlew bootRun

# Unit tests
./gradlew test

# Data / Testcontainers tests
./gradlew dataTest

# Cucumber e2e
./gradlew e2eTest

# Format check
./gradlew spotlessJavaCheck
```

Acceptance for PXG-002: the application starts with `./gradlew bootRun` when Postgres is available and environment variables are set (or local defaults are acceptable).

## Environment variables

Secrets must not be committed. Copy `.env.example` to `.env` for local use (`.env` is gitignored). Spring Boot reads process environment variables; load `.env` via your shell, IDE, or a dotenv helper if you use one.

| Variable | Purpose | Local default (dev only) |
|----------|---------|---------------------------|
| `DB_URL` | JDBC URL | `jdbc:postgresql://localhost:5432/proxigaz` |
| `DB_USER` | DB user | `proxigaz` |
| `DB_PASSWORD` | DB password | `proxigaz` |
| `JWT_SECRET` | HMAC secret for JWT resource server | documented dev placeholder in `application.yaml` — **never use in staging/production** |
| `LIQUIBASE_CONTEXT` | Liquibase contexts | empty (all default changesets) |

Multi-environment profile files (`staging` / `production`) and fail-fast secret checks are **PXG-004**, not this task.

## Related docs

- Sprint plan: `docs/ProxiGaz_Sprint_Plan.md`
- Implementation workflow: `docs/ProxiGaz_Implementation_Guideline.md`
- Security reporting: `SECURITY.md`
- Live progress: `status.md`, `README.md`
