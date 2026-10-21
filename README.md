# ProxiGaz

Backend API for a neighborhood gas-cylinder ordering pilot (Cameroon). Clients request nearby stock; vendors accept/refuse offers; delivery is confirmed with a code. Frontend / PWA is out of scope of this repository’s current plan.

## Status

| Area | State |
|------|--------|
| Manual demand/vendor test | Assumed go — Sprint 0 coding unblocked |
| Sprint 0 — Foundations | In progress (`PXG-001`–`PXG-002` done; next `PXG-003`) |
| Sprints 1–4 | Not started |

Authoritative checklist: [`status.md`](status.md). Sprint breakdown: [`docs/ProxiGaz_Sprint_Plan.md`](docs/ProxiGaz_Sprint_Plan.md). How to work on tasks: [`docs/ProxiGaz_Implementation_Guideline.md`](docs/ProxiGaz_Implementation_Guideline.md).

## What works today

- Hexagonal Spring Boot scaffold with a **Demo** vertical slice (CRUD, Liquibase, OpenAPI-generated API, OAuth2 JWT scopes).
- CI: Spotless + unit / data / e2e tests on GitHub Actions.
- Runtime documented in [`docs/RUNTIME.md`](docs/RUNTIME.md).

Domain features (vendors, geo search, orders, fallback, reviews, admin login) are **not** implemented yet — see `status.md` use-case table.

## Stack

- Java 25, Spring Boot 4.x, Gradle
- PostgreSQL + Liquibase (PostGIS required for upcoming Sprint 0 schema work)
- OpenAPI-first (`specs/openapi/main.yaml`)
- Testcontainers + Cucumber e2e

## Quick start

```bash
cp .env.example .env   # edit secrets as needed; never commit .env
# Ensure PostgreSQL is reachable per DB_* in .env / docs/RUNTIME.md
./gradlew bootRun
```

Tests:

```bash
./gradlew test
./gradlew dataTest
./gradlew e2eTest
```

## Commits

Use Commitizen: `cz c`. Pick the type that matches the change (`docs`, `feat`, `fix`, …). Scope is the task id: e.g. `docs(PXG-002): …`. See the Implementation Guideline.

## Security

How to report vulnerabilities and which lines are supported: [`SECURITY.md`](SECURITY.md).
