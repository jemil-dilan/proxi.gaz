# ProxiGaz — Runtime & Environment

## Chosen runtime

| Choice | Value |
|--------|--------|
| Language | Java 25 (Temurin) |
| Framework | Spring Boot 4.1.x |
| Build | Gradle (Kotlin DSL) via `./gradlew` |
| Persistence | PostgreSQL + PostGIS + Liquibase |
| API contract | OpenAPI-first (`specs/openapi/main.yaml`) |

**Why not NestJS/Node:** PXG-001 already validated a hexagonal (clean) architecture on Spring Boot with contract-first OpenAPI, JPA, Liquibase, and Cucumber e2e. Changing stacks would discard that proven scaffold without benefit for the MVP backend plan.

## How to run

Prerequisites: JDK 25, Docker (for Testcontainers), and a reachable **PostgreSQL + PostGIS** instance for local `bootRun`.

Pinned image for CI / Testcontainers (keep in sync): `postgis/postgis:17-3.5`.

Local DB options:
- Run `docker run --name proxigaz-db -e POSTGRES_USER=proxigaz -e POSTGRES_PASSWORD=proxigaz -e POSTGRES_DB=proxigaz -p 5432:5432 -d postgis/postgis:17-3.5`
- Or any Postgres where you can install PostGIS; Liquibase runs `CREATE EXTENSION IF NOT EXISTS postgis` on migrate

```bash
# Local app (default profile: local — defaults in application-local.yaml)
./gradlew bootRun

# Staging example (secrets required via env — no YAML defaults):
# SPRING_PROFILES_ACTIVE=staging DB_URL=... DB_USER=... DB_PASSWORD=... JWT_SECRET=... ./gradlew bootRun

# Unit tests
./gradlew test

# Data / Testcontainers tests
./gradlew dataTest

# Cucumber e2e
./gradlew e2eTest

# Format check
./gradlew spotlessJavaCheck
```

## Spring profiles

| Profile | File | Secrets / DB defaults |
|---------|------|------------------------|
| `local` (default) | `application-local.yaml` | Dev defaults allowed |
| `staging` | `application-staging.yaml` | **Required** via env — no defaults |
| `production` | `application-production.yaml` | **Required** via env — no defaults |

Central config type: `Configurations` port (`ConfigurationsImpl`) binding `proxigaz.*` properties. Staging/production refuse to start if `JWT_SECRET` is missing, too short, or equal to the local development placeholder.

## Environment variables

Secrets must not be committed. Copy `.env.example` to `.env` for local use (`.env` is gitignored). Spring Boot reads process environment variables; load `.env` via your shell, IDE, or a dotenv helper if you use one.

Application settings use the single `proxigaz` root in `application.yaml` and bind through `ConfigurationsImpl` implementing the `Configurations` port, grouped into typed sections such as `security.jwt` and `admin.bootstrap`.

| Variable | Purpose | Local default (dev only) |
|----------|---------|---------------------------|
| `SPRING_PROFILES_ACTIVE` | Active profile(s) | unset → default `local` |
| `DB_URL` | JDBC URL | `jdbc:postgresql://localhost:5432/proxigaz` (local only) |
| `DB_USER` | DB user | `proxigaz` (local only) |
| `DB_PASSWORD` | DB password | `proxigaz` (local only) |
| `JWT_SECRET` | HMAC secret for JWT resource server | local placeholder in `application-local.yaml` — **never use in staging/production** |
| `PROXIGAZ_SECURITY_JWT_ACCESS_TOKEN_TTL` | Admin JWT lifetime | `15m` |
| `LIQUIBASE_CONTEXT` | Liquibase contexts | empty (all default changesets) |
| `PROXIGAZ_ADMIN_BOOTSTRAP_ENABLED` | Create the initial admin at startup when set to `true` | `false` (unset) |
| `PROXIGAZ_ADMIN_BOOTSTRAP_EMAIL` | Initial admin email; required when bootstrap is enabled | unset |
| `PROXIGAZ_ADMIN_BOOTSTRAP_PASSWORD` | Initial admin password; BCrypt-hashed before storage; required when enabled | unset |

Admin bootstrap is opt-in and idempotent: enable it with credentials supplied through the process environment, start the app once, then disable it and remove the credentials from the environment. No default admin credentials are provided.

## Related docs

- Sprint plan: `docs/ProxiGaz_Sprint_Plan.md`
- Implementation workflow: `docs/ProxiGaz_Implementation_Guideline.md`
- Security reporting: `SECURITY.md`
- Live progress: `status.md`, `README.md`
