# ProxiGaz — Project Status

Last updated: 2026-10-31 (PXG-013 and PXG-014 merged to main; PXG-015 ready for merge; tests not executed)

## Critical Gate

Manual 2–3 week test: **Go; Sprint 0 gate explicitly waived by user direction**. Continue collecting/using pilot numbers for open product decisions (offer duration, Web Push plan B).

## Sprint progress

| Sprint                                | Status      | Notes                                      |
|---------------------------------------|-------------|--------------------------------------------|
| Manual Test (2–3 weeks)               | Go (explicitly waived) | Unblocks Sprint 0 |
| 0 — Foundations & Core Infrastructure | Done | PXG-001–011 complete; manual-test gate explicitly waived |
| 1 — Vendor & Catalog Management       | In progress | PXG-013 and PXG-014 are merged to main. PXG-015 is implemented on `working/PXG-015` and pending merge; its test suites have not been run. Catalog/availability/geo work and sprint release remain |
| 2 — Order Flow (Vertical Slice)       | Not started | Open decision on fallback price rule must be closed before this sprint |
| 3 — Fallback, Delivery & Reviews      | Not started |                                            |
| 4 — Hardening, Admin & Pilot Ready    | Not started |                                            |

## Use case status

| ID       | Name / Description                                      | Implemented | Unit tested | E2E tested | Notes |
|----------|---------------------------------------------------------|-------------|-------------|------------|-------|
| AUTH-01  | Admin login (JWT / session)                             | Implemented | Yes (user-reported) | Yes (user-reported) | Generic invalid-credential response; JWT settings and token ports |
| AUTH-02  | Vendor session validation (long-lived token by admin)   | Implemented | Added, not run | Added, not run | PXG-015 validates hashed bearer tokens; admin rotation/revocation replace or remove the active session. No automatic expiry is configured for long-lived pilot sessions |
| VEN-01   | Admin creates / updates vendor + location (PostGIS)     | Implemented | Yes (PXG-012B, user-reported) | Yes (PXG-012B, user-reported) | JPA persists separate Vendor aggregate and PostGIS location; PXG-014 adds paginated admin listing, merged to main |
| VEN-02   | Admin activates / deactivates vendor                    | Implemented | Added on PXG-013, not run | Added on PXG-013, not run | Dedicated admin status endpoint is merged to main; production nearby-search exclusion remains in PXG-020 |
| VEN-03   | Admin creates long-lived VendorSession                  | Implemented | Added, not run | Added, not run | Rotation replaces the prior session; revocation removes active token record |
| VEN-04   | CRUD VendorBrand (prices with/without exchange)         | No          | No          | No         |       |
| VEN-05   | Availability rule (`is_available AND available_until > now() AND is_open_now`) | Partial | Yes | No | Domain `Availability` VO; query wiring later |
| GEO-01   | Nearby vendors (ST_DWithin + distance sort)             | No          | No          | No         |       |
| ORD-01   | Create order + first OrderOffer + tracking_token + code | No          | No          | No         | Max 2 active + rate limit by IP/device |
| ORD-02   | Vendor accepts / refuses offer                          | No          | No          | No         |       |
| ORD-03   | Offer expiry + fallback (max 3 attempts)                | No          | No          | No         | **Open decision on price rule** |
| ORD-04   | Vendor submits 4-digit confirmation code (max 3 tries)  | No          | No          | No         | → Livrée |
| ORD-05   | Client confirms reception → Livrée & Vérifiée           | No          | No          | No         | Unlocks review |
| ORD-06   | 24 h auto-stats validation (review stays locked)        | No          | No          | No         | Use controlled Clock only |
| REV-01   | Create review (only if Livrée & Vérifiée)               | No          | No          | No         |       |
| SYS-01   | NotificationChannel (Web Push) + plan B                 | No          | No          | No         | Test during manual phase |
| SYS-02   | OtpProvider port (null in pilot)                        | No          | No          | No         | Trigger if fake-order rate > 10 % |
| SYS-03   | OrderReport (fake order) + phone block list + rate limit| No          | No          | No         | Strengthen anti-abuse |
| SYS-04   | Pilot KPI queries                                       | No          | No          | No         |       |
| AUD-01   | Every status change written to order_events             | No          | No          | No         | Mandatory |

## Sprint 0 and Sprint 1 task status

| ID | Task | Status |
|----|------|--------|
| PXG-001 | Repository bootstrap + Demo reference slice | Done |
| PXG-002 | Runtime documented + env management | Done |
| PXG-003 | Database setup (PostgreSQL + PostGIS) | Done |
| PXG-004 | Config & secrets (local / staging / production) | Done |
| PXG-005 | Initial domain migrations + GIST | Done |
| PXG-006 | Domain entities & value objects | Done |
| PXG-007 | Ports definition | Done |
| PXG-008 | Null / in-memory adapters | Done |
| PXG-009 | Admin user model & repository | Done |
| PXG-010 | Admin JWT/session auth | Done |
| PXG-011 | Seed data | Done |
| PXG-012 | Vendor create/update | Create/update baseline implemented; current User/Vendor model alignment completed under PXG-012B |
| PXG-012A | Reconcile User aggregate and vendor profile | Superseded by PXG-012B |
| PXG-012B | Separate Vendor identity and issue onboarding credentials | Implementation complete; user reports unit, data, and E2E suites pass |
| PXG-012C | Docker image and sprint release automation | Planned before the next sprint release |
| PXG-013 | Vendor activate/deactivate | Merged to `main`; tests not run; nearby-search filtering remains in PXG-020 |
| PXG-014 | List/filter Vendors | Merged to `main`; sources compile; tests not run |
| PXG-015 | VendorSession management and validation | Implemented on `working/PXG-015`; sources and test sources compile; tests not run; pending merge |

## Reference Scaffold (PXG-001)

| Component | Implemented | Unit tested | Data tested | E2E tested |
|-----------|-------------|-------------|-------------|------------|
| Demo aggregate + value objects | Yes | Yes | N/A | N/A |
| Demo domain events (Created/Updated/Deleted) | Yes | Yes | N/A | N/A |
| 5 Use Cases (Create/Update/Delete/GetAll/GetById) | Yes | Yes | N/A | N/A |
| Ports (DemoRepository, ReferenceGenerator, DomainEventPublisher) | Yes | Yes | N/A | N/A |
| JPA + Liquibase (t_demo, seq_demo_reference) | Yes | N/A | Yes | Yes |
| DemoController (OAuth2 scope-based) | Yes | N/A | N/A | Yes |
| OpenAPI contract-first (specs/openapi/main.yaml) | Yes | N/A | N/A | Yes |
| RestMapper + DomainExceptionTranslator | Yes | Yes | N/A | Yes |
| Error codes: PROXIGAZ-404-000, PROXIGAZ-409-000, PROXIGAZ-400-000, PROXIGAZ-500-000 | Yes | Yes | N/A | Yes |

## Open decisions (must be closed before coding the related sprint)

1. **Fallback price rule** (before Sprint 2)  
   Option A: Client must explicitly accept the new total when the next vendor has a different price.  
   Option B: Only fall back to vendors whose total ≤ original total.  
   → Decision required before implementing ORD-03.

2. **Offer duration**  
   Current 10–12 min × 3 = up to 36 min is too long for a user who needs gas now.  
   Recommendation to evaluate during/after manual test: shorter window (e.g. 6–8 min) or parallel offers.

3. **Web Push plan B**  
   If the manual-phase test on low-end phones shows Push is unreliable, define a free fallback (e.g. admin dashboard polling + manual call list, or a free Telegram bot, etc.) before relying on it in production code.

## Known gaps / discrepancies found this session

- PXG-010 is pushed on `working/PXG-010` and not yet integrated into `main`; following the user's clarification, PXG-011 is based on `working/PXG-010`.
- The Sprint Plan prerequisite now records that the manual-test gate can be explicitly waived; the user-directed Sprint 0 waiver is reflected above.
- `InMemoryGeoQueryPort` is a test adapter that currently returns registered vendors without radius filtering or distance ordering. Production PostGIS search remains Sprint 1 work (PXG-020).
- PXG-009 completed on `working/PXG-009`: normalized admin email model, repository CRUD adapter, and opt-in BCrypt-backed bootstrap creation.
- PXG-010 completed on `working/PXG-010`, based on `working/PXG-009` by explicit user direction while its MR is open. Unit, PostGIS data, and Cucumber E2E suites passed.
- PXG-010 implements AUTH-01 with credential lookup policy in the application and generic password/token ports. Unit, PostGIS data, and E2E suites passed.
- PXG-010 follow-up consolidates `proxigaz.*` binding in `Configurations` port and `ConfigurationsImpl`, keeps admin bootstrap opt-in and password-hashed, and confirms the disabled runner is not registered.
- PXG-011 adds a local-only Liquibase seed with a BCrypt-backed development admin and five fictional vendor records with PostGIS locations in central Douala. The changeset is selected only by the local, `data_test`, or `e2e` contexts; the production context excludes it.
- PXG-012 added VEN-01 create/update using the vendor schema and PostGIS geography; PXG-013 later added the separate activation/deactivation endpoint and is merged to main.
- PXG-012A's former assessment was premature and is superseded. PXG-012B now uses nullable User email/password fields, separate User and Vendor identities linked by `t_vendors.c_user_id`, matching controller/OpenAPI DTO names, and an atomic onboarding adapter for User, Vendor, and the initial VendorSession. User reports all unit, data, and E2E suites pass; this agent did not rerun them.
- There is no Dockerfile, Compose application deployment, image-publishing job, or deployment workflow. Sprint release requirements are documented in `docs/RELEASE_AND_DEPLOYMENT.md`; PXG-012C is planned to automate the image and staging path.
- PXG-015 implements vendor session validation, rotation, and revocation; adds a runtime Bean Validation provider and vendor request bounds; makes missing vendor updates fail with 404; and defensively copies User roles. New tests were added and compile, but were not run. The branch is ready for merge after review.
- The local profile retains a shared development JWT secret fallback. Staging and production require `JWT_SECRET` and reject weak or placeholder secrets; never deploy using the local profile.
- PXG-002 documents Java/Spring runtime (not Nest); adds `.env.example`, gitignores secrets, `docs/RUNTIME.md`, root `README.md`.
- `SECURITY.md` filled for pre-release support + private vulnerability reporting (was GitHub placeholder).
- Guideline requires Commitizen (`cz c`) and legitimate-only README updates.
- PXG-003: Liquibase `CREATE EXTENSION postgis`; CI/Testcontainers pinned to `postgis/postgis:17-3.5`; data test proves `PostGIS_Version()` / `ST_MakePoint`.
- PXG-004: `Configurations` port with `ConfigurationsImpl`; profiles `local` / `staging` / `production`; fail-fast JWT checks for non-local.
- PXG-005: core tables + GIST on `vendors.location` (`v-1-core-schema.xml`).
- PXG-006: `OrderStatus` state machine, `Money`, `GeoPoint`, `ConfirmationCode`, `TrackingToken`, `Availability` (+ unit tests).
- PXG-007: outbound ports for `Clock`, `OtpProvider`, `NotificationChannel`, and `GeoQueryPort`.
- PXG-008: `SystemClock`/`FakeClock`, null OTP, logging notification, and in-memory geo test adapters.

## Architecture notes relevant to next task

- PostgreSQL + PostGIS are ready. Liquibase v3 consolidates identity in `t_users`; PXG-012B adds the explicit unique `t_vendors.c_user_id` foreign key in v4 while preserving vendor IDs and downstream foreign keys.
- Domain VOs/state machine, ports, base adapters, and AUTH-01 login/JWT exist. PXG-012B's User/Vendor repositories, domain mapping, and API edits pass the user-reported test suites. Vendor onboarding persists User, Vendor, and VendorSession through one transactional adapter. Generic password/token ports and JWT settings use infrastructure beans; `proxigaz.*` settings bind through `Configurations`/`ConfigurationsImpl`. Demo projection selection still passes REST wire codes into application use cases and remains a separate architecture gap.
- Admin bootstrap is opt-in through environment properties; it hashes the supplied password and skips an existing email. PXG-010 owns login/JWT; Sprint 1 PXG-020 owns production PostGIS nearby search.
- Tracking must use `TrackingToken`; availability via `Availability.isCurrentlyAvailable(now)`.
- PXG-013 and PXG-014 are merged to `main`; PXG-015 is the next branch to merge after review.
- All work must follow `docs/ProxiGaz_Implementation_Guideline.md` (including `cz c` commits; PXG-012 was based on PXG-011 by user direction).

## Next recommended action

1. Review and merge `working/PXG-015`; run its unit, data, and E2E suites before merge if that is part of the review gate.
2. Complete the remaining Sprint 1 tasks: PXG-016–021, including catalog/availability and PostGIS nearby search with inactive vendors excluded.
3. PXG-012C — package the service as a Docker image and automate sprint release/deployment to staging.
