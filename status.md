# ProxiGaz — Project Status

Last updated: 2026-11-21 by `working/PXG-062` (PXG-062 complete: ADRs + documentation reconciliation; PXG-061 merged in PR #31; PXG-062 PR pending)

## Gates

Defined in `docs/ProxiGaz_Sprint_Plan.md` (Gates); decision record: [ADR 0001](docs/adr/0001-demand-gate-g1.md).

| Gate | Requirement | Blocks | State |
|------|-------------|--------|-------|
| G1 — Demand | Manual-test numbers meet the Cadrage thresholds, or a dated written waiver with rationale | PXG-022 and all of Sprint 2a | **Waived on 2026-11-18** by the product owner. Rationale: build the MVP first and collect pilot numbers in parallel to settle the open product decisions (offer duration, Web Push plan B). The manual test is not a precondition for Sprint 2a |
| G2 — Push | Result of the Web Push test on real low-end vendor phones recorded here | PXG-030 | **Open** — no result recorded. If the test fails, PXG-030 ships disabled by config and the escalation list (PXG-032) becomes the primary path |

Decisions D1–D20 were ratified on 2026-11-18 as [ADRs 0001–0020](docs/adr/README.md); ADRs 0021–0025 record decisions taken since (Demo kept, `UnitOfWork`, audit trail, contract versioning, logging). Cadrage baseline: **v1.4**.

## Sprint progress

| Sprint                                | Status      | Notes                                      |
|---------------------------------------|-------------|--------------------------------------------|
| Manual Test (2–3 weeks)               | Go (explicitly waived) | Unblocks Sprint 0 |
| 0 — Foundations & Core Infrastructure | Implementation done; staging gate unmet | PXG-001–011 complete; manual-test gate explicitly waived; alpha releases `v0.1.0-alpha.1` and `v0.1.0-alpha.1.0` published; the staging deployment step has never run because no staging host exists |
| 1 — Vendor & Catalog Management       | In progress | PXG-013–015 and Epic 1.0 PXG-055–062 done. Remaining Epic 1.0: PXG-054 (Demo excluded), PXG-063. Catalog/availability/geo (Epic 1.2+) remain |
| 2 — Order Flow (Vertical Slice)       | Not started | Open decision on fallback price rule must be closed before this sprint |
| 3 — Fallback, Delivery & Reviews      | Not started |                                            |
| 4 — Hardening, Admin & Pilot Ready    | Not started |                                            |

## Use case status

| ID       | Name / Description                                      | Implemented | Unit tested | E2E tested | Notes |
|----------|---------------------------------------------------------|-------------|-------------|------------|-------|
| AUTH-01  | Admin login (JWT / session)                             | Implemented | Yes (user-confirmed) | Yes (user-confirmed) | PXG-059: email+IP lockout (5/15m → 429-001 + Retry-After), dummy-hash path, roles on `/me`, password minLength; RateLimiter D17 |
| AUTH-02  | Vendor session validation (long-lived token by admin)   | Implemented | Yes (user-confirmed) | Yes (user-confirmed) | PXG-015 + PXG-058: hashed bearer tokens; soft revoke/rotate; `last_used_at` throttled; inactive vendor → 403-002; identity returns name/isActive |
| VEN-01   | Admin creates / updates vendor + location (PostGIS)     | Implemented | Yes (user-confirmed) | Yes (user-confirmed) | PXG-014 listing + PXG-058 `GET /admin/vendors/{id}` with session metadata and list `sort`; PXG-061 audited (`VENDOR_CREATED`/`VENDOR_UPDATED`) |
| VEN-02   | Admin activates / deactivates vendor                    | Implemented | Yes (CI green, user-confirmed) | Yes (CI green, user-confirmed) | Dedicated admin status endpoint is merged to main; production nearby-search exclusion remains in PXG-020; PXG-061 audited |
| VEN-03   | Admin creates long-lived VendorSession                  | Implemented | Yes (CI green, user-confirmed) | Yes (CI green, user-confirmed) | Rotation replaces the prior session; revocation removes active token record; PXG-061 audited (no token in metadata) |
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
| SYS-01   | NotificationChannel (Web Push) + plan B                 | Partial     | No | No         | RFC 7807 error model implemented (PXG-055); Web Push adapter remains for PXG-030 |
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
| PXG-005 | Initial domain migrations + GIST | Done — delivered with the actual `t_`/`c_` naming; the v1.4 table list (`admin_users`, …) is superseded by PXG-012B; remaining tables are created by the sprint that needs them |
| PXG-006 | Domain entities & value objects | Done |
| PXG-007 | Ports definition | Done |
| PXG-008 | Null / in-memory adapters | Done |
| PXG-009 | Admin user model & repository | Done — admin is a role on the `User` aggregate (`t_users` + `t_user_roles`), not a separate `admin_users` table |
| PXG-010 | Admin JWT/session auth | Done |
| PXG-011 | Seed data | Done |
| PXG-012 | Vendor create/update | Create/update baseline implemented; current User/Vendor model alignment completed under PXG-012B |
| PXG-012A | Reconcile User aggregate and vendor profile | Superseded by PXG-012B |
| PXG-012B | Separate Vendor identity and issue onboarding credentials | Implementation complete; user reports unit, data, and E2E suites pass |
| PXG-012C | Docker image and sprint release automation | Done; Jib images published to Docker Hub + GHCR by the release workflow (last: `v0.1.0-alpha.1.0`, 2026-11-02). The VPS staging files were removed in `2e2ca19` (no staging host); see Gaps |
| PXG-013 | Vendor activate/deactivate | Merged to `main`; CI green per user; nearby-search filtering remains in PXG-020 |
| PXG-014 | List/filter Vendors | Merged to `main`; CI green per user |
| PXG-015 | VendorSession management and validation | Merged to `main`; CI green per user |
| PXG-055 | Error model & RFC 7807 Problem details | Merged to `main` |
| PXG-056 | Security schemes & audience separation | Merged to `main` |
| PXG-057 | Vendor DTO & validation fixes | Merged to `main` |
| PXG-058 | Vendor detail, session metadata, identity enrichment | Merged / complete per prior session |
| PXG-059 | Admin auth hardening | Merged to `main` (PR #28) |
| PXG-060 | Contract CI, SemVer, versioning | Merged to `main` (PR #29) |
| PXG-061 | Admin audit log | Merged to `main` (PR #31); `t_admin_audit_log`, audited vendor writes in one transaction, `GET /admin/audit-log`; server logging (correlation ID, SQL per profile, slow queries) |
| PXG-063 | API scenario scripts | Done on `working/PXG-063` (stacked on PXG-062) for local and CI: Hurl collection `api-scenarios/`, runner with seed reset, CI job `API Scenarios`, on-demand remote workflow. **Staging run pending** (no host) — the sprint plan's done-when is not met yet |
| PXG-062 | Documentation reconciliation & ADRs | Done on `working/PXG-062`; 25 ADRs, gates G1/G2, Cadrage v1.4 baseline, deployment docs aligned with Jib. Awaiting PR |

## Architecture Updates

- **Demo slice preserved**: Added documentation to `docs/ProxiGaz_Implementation_Guideline.md` clarifying that the Demo vertical slice is intentionally preserved as an architecture reference for AI agents and developers.

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

- The earlier status snapshot incorrectly showed PXG-015 as pending merge. PXG-013, PXG-014, and PXG-015 are all merged to `main`; the user confirms CI is green.
- Sprint 0's Docker/GitHub release was performed (alpha releases `v0.1.0-alpha.1` and `v0.1.0-alpha.1.0`), but the release gate's staging deployment has never run: no staging host is provisioned. PXG-012C provides the repeatable image build and publish path.
- The Sprint Plan prerequisite now records that the manual-test gate can be explicitly waived; the user-directed Sprint 0 waiver is reflected above.
- `InMemoryGeoQueryPort` is a test adapter that currently returns registered vendors without radius filtering or distance ordering. Production PostGIS search remains Sprint 1 work (PXG-020).
- PXG-009 completed on `working/PXG-009`: normalized admin email model, repository CRUD adapter, and opt-in BCrypt-backed bootstrap creation.
- PXG-010 completed on `working/PXG-010`, based on `working/PXG-009` by explicit user direction while its MR is open. Unit, PostGIS data, and Cucumber E2E suites passed.
- PXG-010 implements AUTH-01 with credential lookup policy in the application and generic password/token ports. Unit, PostGIS data, and E2E suites passed.
- PXG-010 follow-up consolidates `proxigaz.*` binding in `Configurations` port and `ConfigurationsImpl`, keeps admin bootstrap opt-in and password-hashed, and confirms the disabled runner is not registered.
- PXG-011 adds a local-only Liquibase seed with a BCrypt-backed development admin and five fictional vendor records with PostGIS locations in central Douala. The changeset is selected only by the local, `data_test`, or `e2e` contexts; the production context excludes it.
- PXG-012 added VEN-01 create/update using the vendor schema and PostGIS geography; PXG-013 later added the separate activation/deactivation endpoint and is merged to main.
- PXG-012A's former assessment was premature and is superseded. PXG-012B now uses nullable User email/password fields, separate User and Vendor identities linked by `t_vendors.c_user_id`, matching controller/OpenAPI DTO names, and an atomic onboarding adapter for User, Vendor, and the initial VendorSession. User reports all unit, data, and E2E suites pass; this agent did not rerun them.
- PXG-012C implemented Dockerfile, staging Compose deployment, image-publishing workflow, and automated staging release path.
- PXG-015 implements vendor session validation, rotation, and revocation; adds a runtime Bean Validation provider and vendor request bounds; makes missing vendor updates fail with 404; and defensively copies User roles. It is merged to `main`; CI is green per user confirmation.
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

- PXG-060 (2026-11-18): the PXG-059 Sonar-fix commit had been made on local `main` (unpushed) with scope `PXG-60`; moved to `working/PXG-060` as `refactor(PXG-060)` and local `main` reset to `origin/main`. That commit also failed `spotlessJavaCheck` and broke 6 unit tests and 2 e2e scenarios: it changed the public `GET /admin/vendors` `sort` values to `NAME,ASC`/`CREATED_AT,DESC` (a breaking change, caught by the new oasdiff gate), the generated `SortDTO` enum failed to bind its default value (400), a Mockito matcher was mixed with a raw value, and an e2e assertion compared a boolean to a string. PXG-060 restored the merged inline `sort` parameter (`name,asc`…), the controller/test, the matchers and the assertion; full suite green afterwards.
- PXG-060: `GET /admin/vendors` without a `name` filter failed on PostgreSQL (`function lower(bytea) does not exist`, untyped null parameter). The interim `COALESCE` edit did not fix it; the adapter now always binds an escaped, lower-cased contains pattern. Covered by data tests.
- PXG-060: `/v3/api-docs` and Swagger UI were served in every profile (secured by deny-all). Now disabled by default and in production, public on `local`/`staging`.
- PXG-060 contract fixes: `setVendorStatus` 401 referenced the 422 response; unused `Locked` (423) and `UnprocessableEntity` (422) responses removed until PXG-036 / PXG-016 use them; shared 500 referenced by every operation.
- Open (not PXG-060): `getVendor` documents no 401/403 and returns `VendorDTO` rather than the `VendorDetailDTO` named in the plan; `vendorSessionAuth` declares `bearerFormat: JWT` though tokens are opaque; `bearerAuth`/`oAuth` schemes remain (Demo uses `oAuth`).
- Demo scaffold is kept by owner decision (2026-11-17) despite PXG-054's wording; PXG-054 must exclude Demo.
- PXG-061 (2026-11-18): use cases had no transaction boundary (each adapter was its own `@Transactional`). New `UnitOfWork` port (`SpringUnitOfWork` → `TransactionTemplate`) wraps each audited write; `AuditLogPort.record` is `Propagation.MANDATORY`, so an audit row cannot be written outside the business transaction. Data tests prove a failure after both writes leaves neither, and an audit FK failure rolls back the vendor change.
- PXG-061: admin writes now need a JWT whose subject is the admin's user id (login tokens already are); other subjects get 401 `PROXIGAZ-401-001`. E2E `I am connected with the scopes 'admin'` now signs with the bootstrapped admin's id; `security_audience.feature` writes use it.
- PXG-061: `@Validated` controllers threw an unmapped `ConstraintViolationException` for out-of-range query parameters (e.g. `GET /admin/vendors?size=0` → 500). Now 400 `PROXIGAZ-400-002`.
- PXG-061: `HttpLoggingFilter` now runs before Spring Security, so 401/403 responses are logged and carry a correlation ID; client-supplied `X-Correlation-Id` values are validated (log-injection guard).
- PXG-062 (2026-11-18): D1–D20 ratified as ADRs 0001–0020 with four amendments recorded in the ADRs: D11 (no token lookup prefix), D12 (`c_is_open_now` is still on `t_vendor_brands`; PXG-065 migrates it), D14 (the contract enforces `+237` E.164 for vendor phones, but `PhoneNumber` itself does not and local formats are not normalised), D17 (no proxy, `X-Forwarded-For` ignored). D13 gap: API date-times are UTC but sent without an offset (`java8-localdatetime`); fix before contract milestone C1. ADRs 0021–0025 added.
- PXG-062: deployment docs described files removed in `2e2ca19` (`Dockerfile`, `compose.staging.yaml`, `deploy-staging.sh`); rewritten for Jib. `compose.yaml` still had `build: context: .` and could not start; it now runs the Jib-built `proxigaz:local` image (verified: health 200, admin login 200). **No staging environment exists**, so the release gate's staging step cannot run. `TASK_PXG-012C.md` deleted. The release workflow now records the published image digests in the release notes.
- Open (error codes): duplicate phone and email are not mapped to a domain error. `c_phone` has no unique constraint and a duplicate email violates `t_users`' unique index, so both would surface as an unhandled 500. `PROXIGAZ-409-001`/`409-002` were removed from the registry as unused; the task that implements duplicate detection (PXG-057 follow-up or PXG-064) must add them back per `docs/ERROR_CODES.md` → Adding New Error Codes. A registry/enum parity test (`ErrorCodesDocumentationTest`) is added on `chore/error-registry`.
- Open (not PXG-061): `UserController.getAdminProfile` still parses the JWT subject itself (returns a bare 401 without problem body); align it with `AuthenticatedAdmin` later. `LoggingNotificationChannel` logs client phone numbers at INFO — revisit before the pilot (PXG-030).

## Architecture notes relevant to next task

- PostgreSQL + PostGIS are ready. Liquibase v3 consolidates identity in `t_users`; PXG-012B adds the explicit unique `t_vendors.c_user_id` foreign key in v4 while preserving vendor IDs and downstream foreign keys.
- Domain VOs/state machine, ports, base adapters, and AUTH-01 login/JWT exist. PXG-012B's User/Vendor repositories, domain mapping, and API edits pass the user-reported test suites. Vendor onboarding persists User, Vendor, and VendorSession through one transactional adapter. Generic password/token ports and JWT settings use infrastructure beans; `proxigaz.*` settings bind through `Configurations`/`ConfigurationsImpl`. Demo projection selection still passes REST wire codes into application use cases and remains a separate architecture gap.
- Admin bootstrap is opt-in through environment properties; it hashes the supplied password and skips an existing email. PXG-010 owns login/JWT; Sprint 1 PXG-020 owns production PostGIS nearby search.
- Tracking must use `TrackingToken`; availability via `Availability.isCurrentlyAvailable(now)`.
- PXG-013–015 and PXG-055–061 are merged. PXG-060 adds the `OpenAPI Contract` CI job (`scripts/validate-openapi.sh`, `.spectral.yaml`, `scripts/check-openapi-breaking.sh`); every new endpoint must lint clean (operationId, at least one 4xx, 500 via `components/responses/InternalServerError`, no unused components, `required` names declared).
- Contract `info.version` stays `0.1.0-dev`; releases stamp the tag version via `scripts/prepare_openapi_release.py`. Breaking changes need the PR label `contract-breaking-approved`.
- PXG-061: every new admin write use case must take the admin `UserId`, run inside `UnitOfWork.inTransaction`, and record an `AuditEntry` (allowlisted `AuditMetadata` only). PXG-064 brand CRUD adds `AuditTargetType.BRAND` and its actions.
- Remaining Epic 1.0: PXG-054 (keep Demo), PXG-063. Epic 1.2+ (catalog/availability/geo) remains.
- Decisions live in `docs/adr/`. When a task lands, update the "Implementation state" of the ADRs it touches; change a decision only by a dated amendment or a superseding ADR.
- All work must follow `docs/ProxiGaz_Implementation_Guideline.md` (including `cz c` commits).

## Next recommended action

1. Open and merge the `working/PXG-062` PR (PXG-061 merged in #31). The follow-up branch `chore/error-registry` (Qodana fixes, registry trim, registry parity test) should be reviewed and merged separately.
2. Choose and provision a staging environment (requirements in `docs/RELEASE_AND_DEPLOYMENT.md` → Pilot deployment). Without it, no sprint release can be completed under the release policy, and PXG-063's runner has nothing to run against.
3. When staging exists: add the `PROXIGAZ_SCENARIO_ADMIN_*` secrets to the GitHub `staging` environment and run `API scenarios (remote)` to close PXG-063.
4. Continue Epic 1.0: PXG-054 (template leftovers, keeping Demo). Each of PXG-064–067 extends `api-scenarios/sprint-1/` (brand, availability, nearby, quote).
5. Complete Epic 1.2+ (PXG-064, PXG-016, PXG-065, PXG-017, PXG-020/021, PXG-066/067).
