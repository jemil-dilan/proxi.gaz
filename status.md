# ProxiGaz — Project Status

Last updated: 2026-10-25 (working/PXG-010 configuration follow-up)

## Critical Gate

Manual 2–3 week test: **Go; Sprint 0 gate explicitly waived by user direction**. Continue collecting/using pilot numbers for open product decisions (offer duration, Web Push plan B).

## Sprint progress

| Sprint                                | Status      | Notes                                      |
|---------------------------------------|-------------|--------------------------------------------|
| Manual Test (2–3 weeks)               | Go (explicitly waived) | Unblocks Sprint 0 |
| 0 — Foundations & Core Infrastructure | In progress | PXG-001–010 done; next PXG-011 |
| 1 — Vendor & Catalog Management       | Not started |                                            |
| 2 — Order Flow (Vertical Slice)       | Not started | Open decision on fallback price rule must be closed before this sprint |
| 3 — Fallback, Delivery & Reviews      | Not started |                                            |
| 4 — Hardening, Admin & Pilot Ready    | Not started |                                            |

## Use case status

| ID       | Name / Description                                      | Implemented | Unit tested | E2E tested | Notes |
|----------|---------------------------------------------------------|-------------|-------------|------------|-------|
| AUTH-01  | Admin login (JWT / session)                             | Yes         | Yes         | Yes        | Generic credentials errors; JWT settings and token ports; full unit, data, and E2E checks pass |
| AUTH-02  | Vendor session validation (long-lived token by admin)   | No          | No          | No         |       |
| VEN-01   | Admin creates / updates vendor + location (PostGIS)     | No          | No          | No         |       |
| VEN-02   | Admin activates / deactivates vendor                    | No          | No          | No         |       |
| VEN-03   | Admin creates long-lived VendorSession                  | No          | No          | No         |       |
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

## Sprint 0 task status

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
| PXG-011 | Seed data | Not started |

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

- README and prior status notes pointed to PXG-007/PXG-008 as next, but both are implemented; corrected to PXG-009.
- The Sprint Plan prerequisite now records that the manual-test gate can be explicitly waived; the user-directed Sprint 0 waiver is reflected above.
- `InMemoryGeoQueryPort` is a test adapter that currently returns registered vendors without radius filtering or distance ordering. Production PostGIS search remains Sprint 1 work (PXG-020).
- PXG-009 completed on `working/PXG-009`: normalized admin email model, repository CRUD adapter, and opt-in BCrypt-backed bootstrap creation.
- PXG-010 completed on `working/PXG-010`, based on `working/PXG-009` by explicit user direction while its MR is open. Unit, PostGIS data, and Cucumber E2E suites passed.
- PXG-010 implements AUTH-01 with credential lookup policy in the application and generic password/token ports. Unit, PostGIS data, and E2E suites passed.
- PXG-010 follow-up consolidates `proxigaz.*` binding in `Configurations` port and `ConfigurationsImpl`, keeps admin bootstrap opt-in and password-hashed, and confirms the disabled runner is not registered.
- Architecture review gaps remain in committed code: Demo retrieval use cases still consume REST constants and extraction strings, Demo value objects do not enforce the documented column bounds, `AdminUser.id()` exposes a UUID, and `SecurityConfig` contains bean definitions despite the `*Beans` naming convention.
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

- PostgreSQL + PostGIS ready; core domain tables exist (`t_*` schema), including `t_admin_users`.
- Domain VOs/state machine, outbound ports, base adapters, admin user persistence, and AUTH-01 admin login/JWT are implemented. Generic password/token ports and the JWT settings port are wired through infrastructure beans. All `proxigaz.*` settings bind through the `Configurations` port implemented by `ConfigurationsImpl`. Demo projection selection still passes REST wire codes into application use cases and needs a follow-up boundary cleanup.
- Admin bootstrap is opt-in through environment properties; it hashes the supplied password and skips an existing email. PXG-010 owns login/JWT; Sprint 1 PXG-020 owns production PostGIS nearby search.
- Tracking must use `TrackingToken`; availability via `Availability.isCurrentlyAvailable(now)`.
- All work must follow `docs/ProxiGaz_Implementation_Guideline.md` (including `cz c` commits; branch from `main`).

## Next recommended action

1. PXG-011 — development seed data, including pilot vendors.
