# ProxiGaz

Backend API for a neighborhood gas-cylinder ordering pilot (Cameroon). Clients request nearby stock; vendors accept/refuse offers; delivery is confirmed with a code. Frontend / PWA is out of scope of this repository’s current plan.

## Status

| Area | State |
|------|--------|
| Manual demand/vendor test | Go by explicit user waiver — Sprint 0 coding unblocked |
| Sprint 0 — Foundations | In progress (`PXG-001`–`PXG-010` done; next `PXG-011`) |
| Sprints 1–4 | Not started |

Authoritative checklist: [`status.md`](status.md). Sprint breakdown: [`docs/ProxiGaz_Sprint_Plan.md`](docs/ProxiGaz_Sprint_Plan.md). How to work on tasks: [`docs/ProxiGaz_Implementation_Guideline.md`](docs/ProxiGaz_Implementation_Guideline.md).

## What works today

- Hexagonal Spring Boot scaffold with a **Demo** vertical slice (CRUD, Liquibase, OpenAPI-generated API, OAuth2 JWT scopes).
- PostgreSQL **+ PostGIS** (Liquibase enables the extension; CI/tests use `postgis/postgis:17-3.5`).
- Core domain tables migrated (vendors with GIST location, orders/offers, admin_users, etc.).
- Admin user domain model and repository persist normalized emails and BCrypt password hashes.
- Domain VOs: order status state machine, Money (FCFA), GeoPoint, ConfirmationCode, TrackingToken, Availability.
- Config profiles: `local` (default), `staging`, `production` — secrets via env; see [`docs/RUNTIME.md`](docs/RUNTIME.md).
- Optional initial admin creation at startup uses environment-provided credentials and stores a BCrypt password hash; see [`docs/RUNTIME.md`](docs/RUNTIME.md).
- CI: Spotless + unit / data / e2e tests on GitHub Actions.

Admin login (AUTH-01) is implemented. Vendor management and authentication, production geo search, orders, fallback, and reviews are **not** implemented yet — see `status.md` use-case table.

## Stack

- Java 25, Spring Boot 4.x, Gradle
- PostgreSQL + PostGIS + Liquibase
- OpenAPI-first (`specs/openapi/main.yaml`)
- Testcontainers + Cucumber e2e

## Quick start

```bash
cp .env.example .env   # edit secrets as needed; never commit .env
# Start PostGIS locally, e.g.:
# docker run --name proxigaz-db -e POSTGRES_USER=proxigaz -e POSTGRES_PASSWORD=proxigaz \
#   -e POSTGRES_DB=proxigaz -p 5432:5432 -d postgis/postgis:17-3.5
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
