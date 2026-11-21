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

The local Docker Compose stack runs the application image and PostGIS with persistent local data. Build the image with Jib first (there is no Dockerfile); the app needs about 35 s to answer, and the image has no Docker health check, so poll `/actuator/health`:

```bash
./gradlew jibDockerBuild -Djib.to.image=proxigaz:local
docker compose up -d
curl localhost:8080/actuator/health
docker compose down
```

The stack uses development-only credentials and seed data. `docker compose down -v` deletes the local database volume.

### API scenarios

`api-scenarios/` holds versioned [Hurl](https://hurl.dev) scenarios (`sprint-N/*.hurl`) and one environment file per target (`env/local.env`, `env/staging.env`). `scripts/run-api-scenarios.sh <env>` waits for `/actuator/health`, then runs every scenario with a unique vendor name and phone, so it can be repeated against shared data; it writes a JUnit report to `build/api-scenarios/junit.xml`. It uses a local `hurl` (`scripts/install-hurl.sh`, Hurl 8.1.0) when that binary runs, otherwise the official Hurl image pinned by digest. The prebuilt Linux binary needs `libxml2.so.2`; Ubuntu 26.04 ships `libxml2.so.16`, so there the Docker image is used.

| Variable | Purpose |
|----------|---------|
| `PROXIGAZ_SCENARIO_BASE_URL` | Overrides `base_url` of the environment file |
| `PROXIGAZ_SCENARIO_ADMIN_EMAIL` / `PROXIGAZ_SCENARIO_ADMIN_PASSWORD` | Admin account used by the scenario; required for staging (never committed) |
| `PROXIGAZ_SCENARIO_HEALTH_TIMEOUT` | Seconds to wait for the health check (default 180; raise it for hosts that sleep when idle) |

`--reset-local` rebuilds the image and restarts the local stack on a fresh database (the seed-reset step). CI runs the scenarios on every PR (job `API Scenarios`); `.github/workflows/api-scenarios.yml` runs them on demand against a deployed environment using the `PROXIGAZ_SCENARIO_ADMIN_*` secrets of that GitHub environment.

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
| `PROXIGAZ_ADMIN_BOOTSTRAP_ENABLED` | Create the initial admin at startup when set to `true` | `false` (unset) |
| `PROXIGAZ_ADMIN_BOOTSTRAP_EMAIL` | Initial admin email; required when bootstrap is enabled | unset |
| `PROXIGAZ_ADMIN_BOOTSTRAP_PASSWORD` | Initial admin password; BCrypt-hashed before storage; required when enabled | unset |
| `PROXIGAZ_SLOW_QUERY_MS` | Queries slower than this many milliseconds are logged on `org.hibernate.SQL_SLOW` | `500` |
| `LOGGING_LEVEL_<LOGGER>` | Raise any logger without a redeploy, e.g. `LOGGING_LEVEL_ORG_HIBERNATE_SQL=DEBUG` | see [Logging](#logging) |

## Logging

Every log line carries the request correlation ID in brackets, e.g. `[9ab8fb4a-…]`. The same ID is returned in the `X-Correlation-Id` response header and in every problem response, so one request can be followed from the HTTP line through its SQL to any warning. A client may send its own `X-Correlation-Id` (letters, digits, `.`, `_`, `-`, at most 64 characters); anything else is replaced.

| What | `local` | `staging` | `production` |
|------|---------|-----------|--------------|
| Request and response line (`HttpLoggingFilter`): 4xx at WARN, 5xx at ERROR | yes | yes | yes |
| Slow queries (`org.hibernate.SQL_SLOW`, threshold `PROXIGAZ_SLOW_QUERY_MS`) | yes | yes | yes |
| Every SQL statement, with `?` placeholders (`org.hibernate.SQL`, `JdbcTemplate`) | yes, formatted | yes | off |
| SQL bind values (`org.hibernate.orm.jdbc.bind`) | yes | **never** | **never** |
| Transaction begin/commit/rollback (`JpaTransactionManager`) | yes | off | off |
| Application DEBUG (`cm.jemil.proxi.gaz`) | yes | off | off |

Bind values contain password hashes, session-token hashes and phone numbers, so they are logged only on `local`; `LoggingConfigurationTest` fails if a shared profile enables them. To debug production temporarily, set `LOGGING_LEVEL_ORG_HIBERNATE_SQL=DEBUG` and restart, then remove it.

## Container and release workflow

- Images are built with **Jib** (`./gradlew jib` / `jibDockerBuild`; no Dockerfile): base `eclipse-temurin:25-jre`, non-root user `10001`, port `8080`, `-XX:MaxRAMPercentage=75.0`. The version comes from `-PappVersion`.
- `compose.yaml` is the local app + PostGIS stack. There is no staging compose file or deploy script; staging hosting is still open.
- `.github/workflows/release.yml` is manually dispatched from `main`. It reruns formatting, all test suites and the contract checks, publishes the image to Docker Hub and GHCR (version and `latest` tags), records the image digests in the release notes, then waits for the `release-approval` environment before creating the annotated Git tag and GitHub Release with the versioned OpenAPI file.
- GitHub configuration, staging requirements, backup policy, and rollback behaviour are documented in `docs/RELEASE_AND_DEPLOYMENT.md`.

The `local` Liquibase context seeds a development admin and five fictional central Douala vendors. `data_test` and `e2e` also opt into this seed for acceptance coverage; `staging` and `production` use their own contexts and exclude it. The local seed credentials are documented in the root README and must only be used for local development.

Admin bootstrap is opt-in and idempotent: enable it with credentials supplied through the process environment, start the app once, then disable it and remove the credentials from the environment. No environment-provided default admin credentials are configured.

Vendor authentication uses the opaque token returned once during onboarding (or session rotation). Send it as `Authorization: Bearer <token>` to `/vendor/session`; a successful response includes vendor `name` and `isActive`. Tokens are stored as SHA-256 hashes; rotation/revocation soft-sets `revoked_at`. Invalid/revoked tokens return **401 `PROXIGAZ-401-001`**; a valid token for an inactive vendor returns **403 `PROXIGAZ-403-002`**. Successful vendor requests update `last_used_at` at most once per minute. Responses that return a raw token (`POST /admin/vendors`, `POST …/session`) include `Cache-Control: no-store`. Admin operations continue to use the admin JWT.

## Related docs

- Sprint plan: `docs/ProxiGaz_Sprint_Plan.md`
- Implementation workflow: `docs/ProxiGaz_Implementation_Guideline.md`
- Sprint releases and deployment: `docs/RELEASE_AND_DEPLOYMENT.md`
- Security reporting: `SECURITY.md`
- Live progress: `status.md`, `README.md`
