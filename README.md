# ProxiGaz

Backend API for a neighborhood gas-cylinder ordering pilot (Cameroon). Clients request nearby stock; vendors accept/refuse offers; delivery is confirmed with a code. Frontend / PWA is out of scope of this repository’s current plan.

## Status

| Area | State |
|------|--------|
| Manual demand/vendor test | Go by explicit user waiver — Sprint 0 coding unblocked |
| Sprint 0 — Foundations | Done (`PXG-001`–`PXG-011`) |
| Sprint 1 — Vendor & Catalog | In progress (`PXG-012B` implementation complete; user reports unit, data, and E2E suites pass; review follow-ups and sprint release remain) |
| Sprints 2–4 | Not started |

Authoritative checklist: [`status.md`](status.md). Sprint breakdown: [`docs/ProxiGaz_Sprint_Plan.md`](docs/ProxiGaz_Sprint_Plan.md). How to work on tasks: [`docs/ProxiGaz_Implementation_Guideline.md`](docs/ProxiGaz_Implementation_Guideline.md).

## What works today

- Hexagonal Spring Boot scaffold with a **Demo** vertical slice (CRUD, Liquibase, OpenAPI-generated API, OAuth2 JWT scopes).
- PostgreSQL **+ PostGIS** (Liquibase enables the extension; CI/tests use `postgis/postgis:17-3.5`).
- Core domain tables migrated, including the shared `t_users` identity and role tables, vendor profiles with GIST-indexed locations, orders/offers, and related records.
- The User aggregate owns account identity, roles, optional normalized email, and admin password hashes. Vendor is a separate aggregate linked one-to-one to User by `t_vendors.c_user_id`.
- Admins can create and update vendors through `POST /admin/vendors` and `PUT /admin/vendors/{vendorId}`. Onboarding returns a one-time, cryptographically random vendor session token; only its SHA-256 hash is stored. Vendor locations persist as PostGIS geography points. Vendor session authentication, rotation, and revocation are not implemented yet, so the token cannot currently authorize vendor requests.
- Domain VOs: order status state machine, Money (FCFA), GeoPoint, ConfirmationCode, TrackingToken, Availability.
- Config profiles: `local` (default, development defaults allowed), `staging`, `production` (secrets required via env); see [`docs/RUNTIME.md`](docs/RUNTIME.md).
- Optional initial admin creation at startup uses environment-provided credentials and stores a BCrypt password hash; see [`docs/RUNTIME.md`](docs/RUNTIME.md).
- The `local` Liquibase context seeds a development-only admin (`admin@proxigaz.local` / `password`) and five fictional vendors in central Douala. Seed data is excluded from staging and production contexts.
- CI: Spotless + unit / data / e2e tests on GitHub Actions.

Admin login (AUTH-01) and vendor create/update (VEN-01) are implemented. The user reports unit, data, and E2E suites pass on this worktree; they were not rerun during this review. Vendor session validation/rotation, catalog management, production geo search, orders, fallback, and reviews are **not** implemented yet — see `status.md` use-case table.

Sprint delivery policy: every sprint closes with a staging deployment, release notes, and an annotated SemVer tag. Production promotion follows acceptance. See [`docs/RELEASE_AND_DEPLOYMENT.md`](docs/RELEASE_AND_DEPLOYMENT.md); Docker image build and deployment automation is planned as PXG-012C.

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

The default `local` profile applies the local seed data. The seeded account and vendors are for local development only.

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
