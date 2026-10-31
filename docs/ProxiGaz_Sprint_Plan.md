# ProxiGaz — Backend Sprint Plan (MVP)

**Version:** 2.0
**Date:** 2026-10-31
**Based on:** Cadrage v1.4 (plan v1.4 cited v1.5 — see PXG-062) + MVP Specification v1.2 + `ProxiGaz_Project_Analysis.md` v1.0 (decisions D1–D20)
**Language:** English
**Scope:** Backend only (API, domain, persistence, jobs, admin endpoints). The PWA is a separate track; this plan publishes **contract milestones** so that it can run in parallel.
**Rules:** All work follows `ProxiGaz_Implementation_Guideline.md` and `docs/RELEASE_AND_DEPLOYMENT.md`.

---

## How to read this plan

- **Epic** = a deliverable capability. Each epic has one or more **User Stories** with **Acceptance Criteria** (Given / When / Then), then the **Tasks** that implement them.
- **Task IDs are stable.** Existing IDs (PXG-001…053) keep their meaning; new tasks continue from **PXG-054**. Tasks are not renumbered even when they move between sprints.
- **Status:** ✅ done · 🔧 in progress · ⬜ to do · ❓ cannot be verified from the files I have · ⛔ removed.
- **Estimate:** S ≤ 0.5 d · M ≈ 1 d · L ≈ 2–3 d · anything bigger must be split.
- **Test notation:** U = unit, I = integration (real PostgreSQL + PostGIS, e.g. Testcontainers), E = end-to-end over HTTP, C = concurrency.

## What changed from v1.4

| Change | Reason |
|---|---|
| New **Epic 1.0** (contract & foundation cleanup, PXG-054…063) | Fix contract bugs before more endpoints depend on them |
| Epic 1.2/1.3 extended: brand reference table, vendor self-service, public nearby, quote | Missing for the client and vendor journeys |
| Sprint 2 split into **2a** (slice) and **2b** (push, expiry, fallback) | Sprint 2 was 13 tasks / 2 weeks and the spec places fallback later |
| PXG-018 ⛔ removed, PXG-012A ⛔ removed, PXG-019 moved to Sprint 3 | Query-time truth; no data in Sprint 1 to score |
| Cancel, withdraw, stale-order sweeper, code lock, report triage added | State machine had terminal states nothing could reach |
| Race tests moved to Sprint 2b (PXG-073); PXG-044 reduced to idempotency | Concurrency is a Sprint 2 risk, not a Sprint 4 one |
| Epic 4.3 Security & privacy added | OWASP API review, data retention |
| Gates G1/G2, contract milestones, demo scripts | Make the slice demonstrable without a frontend |

## Gates and global rules

| Gate | Rule | Blocks |
|---|---|---|
| **G1 — Demand gate** | Manual-test numbers meet the cadrage thresholds, **or** a dated written waiver with rationale is recorded in `status.md` | PXG-022 and everything after (Sprint 2a) |
| **G2 — Push gate** | Result of the Web Push test on real low-end vendor phones is recorded in `status.md`. If unusable, PXG-030 is replaced by a manual-escalation-only channel and the escalation list becomes the primary path | PXG-030 |

### Definition of Done (every task)

1. Code reviewed (PR), CI green (lint, tests, contract lint).
2. Tests at the level stated in the task; migrations have a tested rollback.
3. OpenAPI updated **in the same PR**; no endpoint without documented 2xx/4xx and problem codes.
4. Every order/offer state change writes an `order_events` row (actor, from → to).
5. Logs carry a correlation ID and contain no raw tokens, no full phone numbers.
6. `status.md` updated when a task closes an exit criterion.

### Global non-functional requirements

- Tracking tokens: ≥ 128 bits random, stored hashed (D10). Vendor tokens: ≥ 256 bits, stored hashed (D11).
- Money = integer FCFA (XAF). Time = UTC `TIMESTAMPTZ`; `:now` always comes from the `Clock` port (D13).
- Phones normalised to E.164 `+237…` (D14). Errors are `application/problem+json` with `PROXIGAZ-{HTTP}-{NNN}` codes (D15).
- Runtime configuration (environment, D20): `OFFER_TTL_SECONDS=600`, `OFFER_MAX_ATTEMPTS=3`, `ESCALATION_AFTER_SECONDS=300`, `SEARCH_RADIUS_METERS=5000`, `SEARCH_RADIUS_MAX_METERS=15000`, `STALE_EN_COURS_HOURS=6`, `AUTO_CLOSE_LIVREE_HOURS=24`, `DATA_RETENTION_DAYS=180`.
- Every sprint closes with a staging deployment of a tagged image, release notes and an annotated SemVer tag (PXG-012C). Production promotion is approval-gated **from the pilot onwards**.

### Architecture (unchanged, must be respected)

```
domain/          pure business logic (entities, value objects, state machines, domain services)
application/     use cases + ports (interfaces)
infrastructure/  adapters (PostgreSQL/PostGIS, Web Push, rate limiter, future OTP)
interfaces/      HTTP controllers, DTOs, validation
jobs/            workers (offer expiry, stale-order sweeper, retention)
```

Ports: `OtpProvider`, `NotificationChannel`, `GeoQueryPort`, `Clock`, plus **`RateLimiter`** (new, D17). Aggregates: User, Vendor, VendorBrand, Order, OrderOffer, OrderEvent, OrderReport, Review, VendorSession, PushSubscription.

### Contract milestones for the frontend

| Milestone | Delivered at | Frontend can build |
|---|---|---|
| **C1** | End of Sprint 1 | Admin login + vendor management, vendor availability screen, client nearby + quote screens |
| **C2** | End of Sprint 2a | Client order + tracking, vendor offer list + accept/refuse (polling) |
| **C3** | End of Sprint 2b | Vendor push subscription, admin escalation list |
| **C4** | End of Sprint 3 | Delivery with code, client confirmation + review, reports, KPI dashboard |
| **C5** | End of Sprint 4 | Admin order investigation, overrides, onboarding checklist |

---

## Sprint 0 — Foundations & Core Infrastructure (✅ assumed complete)

**Goal:** Runnable skeleton, database, domain types, ports, admin authentication.
**Duration (actual/estimate):** 1–2 weeks. Kept here for traceability; verify each item against `status.md`.

### Epic 0.1 — Project Bootstrap ✅

**US-0.1.1 — Reproducible project.** *As the developer, I want a repository that builds, lints, tests and starts in one command, so that every sprint starts from a working baseline.*

Acceptance criteria
- AC1 — Given a fresh clone, when I run the documented start command with a valid env file, then the app starts and connects to an empty PostgreSQL + PostGIS database.
- AC2 — Given a pull request, when CI runs, then lint and tests execute and a failure blocks merge.
- AC3 — Given three environment files (local, staging, production), when I switch them, then the app picks the right configuration and no secret is committed.

| ID | Status | Task | Details | Est. |
|---|---|---|---|---|
| PXG-001 | ✅ | Repository & tooling | Repo layout, package manager, linter, formatter, CI (lint + test) | M |
| PXG-002 | ✅ | Runtime | Framework chosen and documented, env variable management, start command | S |
| PXG-003 | ✅ | Database | PostgreSQL + PostGIS, migration tool, connection pool, local container | M |
| PXG-004 | ✅ | Config & secrets | Central typed config, per-environment files, validation at boot (fail fast) | S |

### Epic 0.2 — Core Schema & Domain Foundations ✅

**US-0.2.1 — Domain safety net.** *As the developer, I want the order state machine, money, geo and availability rules implemented as pure, tested code, so that features can be added without breaking business rules.*

Acceptance criteria
- AC1 — Given the migrations, when applied to an empty database, then PostGIS is enabled and the vendor location has a GIST index.
- AC2 — Given the domain layer, when I run unit tests, then no database or network is needed.
- AC3 — Given a port (`Clock`, `NotificationChannel`, `OtpProvider`, `GeoQueryPort`, `RateLimiter`), when tests run, then a null/fake adapter is injected.

| ID | Status | Task | Details | Est. |
|---|---|---|---|---|
| PXG-005 | ✅ | Initial migrations | Delivered with the **actual** naming (`t_`/`c_`). The v1.4 table list (`admin_users`, …) is superseded by PXG-012B (User root, Vendor aggregate). Remaining tables are created **by the sprint that needs them** (Analysis §4.3) | L |
| PXG-006 | ✅ | Domain entities & value objects | OrderStatus + helpers, Money, GeoPoint, ConfirmationCode, TrackingToken, Availability. *Extended by PXG-069* | L |
| PXG-007 | ✅ | Ports | `OtpProvider`, `NotificationChannel`, `GeoQueryPort`, `Clock`. *`RateLimiter` added in PXG-023* | S |
| PXG-008 | ✅ | Null / in-memory adapters | NullOtpProvider, logging channel, SystemClock + FakeClock | M |

### Epic 0.3 — Admin Authentication ✅

**US-0.3.1 — Admin sign-in.** *As an admin, I want to log in with email and password, so that only I can operate vendors and orders.*

Acceptance criteria
- AC1 — Given valid credentials, when I POST `/admin/auth/login`, then I receive a bearer token and its lifetime.
- AC2 — Given invalid credentials, then I receive 401 without revealing which field was wrong.
- AC3 — Given no/invalid token, when I call an admin route, then I receive 401.

| ID | Status | Task | Details | Est. |
|---|---|---|---|---|
| PXG-009 | ✅ | Admin user model | Implemented on the `User` aggregate with an Admin role (not a separate `admin_users` table) | M |
| PXG-010 | ✅ | Admin JWT auth | Login, `/admin/auth/me`, hashing (argon2id/bcrypt). *Hardened by PXG-059* | M |
| PXG-011 | ✅ | Seed data | 1 admin + 5–10 vendors with points in the pilot neighbourhood | S |

**Sprint 0 exit:** migrations green · admin authenticates · state machine unit-tested · ports + null adapters · `status.md` exists.

---

## Sprint 1 — Vendor & Catalog Management + Public Discovery
**Goal:** Admin manages vendors, brands and availability; vendors can maintain their own availability; clients can discover nearby vendors and get a total price. The contract is clean and versioned.
**Duration estimate (remaining):** ~1.5 weeks. Execution order: **Epic 1.0 → 1.1 (finish PXG-012C) → 1.2 → 1.3**.
**Milestone:** contract **C1**.

### Epic 1.0 — Contract & Foundation Cleanup (new, execute first)

**US-1.0.1 — A trustworthy contract.** *As the frontend developer, I want a clean, consistent and versioned OpenAPI contract with a uniform error model, so that I can generate a client and never guess response shapes.*

Acceptance criteria
- AC1 — Given `main.yaml`, when the linter runs in CI, then there are no errors (no undefined required properties, no unused schemas, every operation has an `operationId` and documented error responses).
- AC2 — Given any 4xx/5xx response, then the body is `application/problem+json` with `type`, `title`, `status`, `code` (`PROXIGAZ-{HTTP}-{NNN}`), `correlationId`, and `errors[]` for validation failures.
- AC3 — Given an admin endpoint, then 401 (missing/invalid token) and 403 (wrong audience) are documented and enforced; an admin JWT never authenticates a vendor route and vice-versa.
- AC4 — Given the contract, then no template leftovers (`Demo`, `oAuth`, `fieldsToExtractCode`) remain.
- AC5 — Given a release, then `info.version` equals the SemVer tag and the `servers` list includes staging.

**US-1.0.2 — Accountable admin actions.** *As the operator, I want every sensitive admin action recorded, so that I can explain who changed what.*

Acceptance criteria
- AC1 — Given an admin creates/updates/activates a vendor or rotates/revokes a session, then a row is written to `t_admin_audit_log` (admin, action, target, metadata) in the same transaction.
- AC2 — Given the audit log, then it never contains a raw session token or password.

**US-1.0.3 — Demonstrable backend.** *As the product owner, I want a scripted API scenario per sprint, so that I can demo progress and the frontend can see real payloads without a UI.*

Acceptance criteria
- AC1 — Given a clean staging database with seed data, when I run the scenario collection, then every step passes and the final state is asserted.

#### Tasks

**PXG-054 ⬜ Remove template leftovers** (S)
- Delete `/demo` endpoints, `DemoDTO`, `DemoCreateDTO`, `DemoUpdateDTO`, `CreationResponseDTO` (if unused), the `oAuth` security scheme and the `Demo` tag, plus the matching code, tests and seed.
- Done when: a repo-wide search for `Demo`/`oAuth` returns nothing; contract lint passes.

**PXG-055 ⬜ Error model & code registry** (M)
- Add `components/schemas/Problem` (RFC 7807 + `code`, `correlationId`, `errors[{field, message}]`) and reusable `components/responses` for 400, 401, 403, 404, 409, 422, 423, 429, 500; reference them from every operation.
- Create `docs/ERROR_CODES.md`: `PROXIGAZ-400-001 VALIDATION_FAILED`, `401-001 TOKEN_INVALID`, `403-001 FORBIDDEN_AUDIENCE`, `403-002 VENDOR_INACTIVE`, `404-001 RESOURCE_NOT_FOUND`, `409-001 DUPLICATE_PHONE`, `409-002 DUPLICATE_EMAIL`, `429-001 RATE_LIMITED`, `500-001 INTERNAL_ERROR`. Later sprints append (e.g. `409-010 MAX_ACTIVE_ORDERS`, `409-011 PRICE_CHANGED`, `409-012 OFFER_NOT_ACTIONABLE`, `423-001 CODE_LOCKED`).
- Global exception mapper; stack traces never leave the server.
- Tests: I — one per mapped error; contract test that all documented codes exist in the registry.

**PXG-056 ⬜ Security schemes & audience separation** (M)
- Replace `bearerAuth` by `adminAuth` (HTTP bearer, JWT) and `vendorSessionAuth` (HTTP bearer, opaque). Apply per route group (`/admin/**` → adminAuth, `/vendor/**` → vendorSessionAuth, public routes `security: []`).
- JWT carries an audience claim (`admin`); vendor guard rejects JWTs, admin guard rejects opaque tokens.
- Tests: E — admin token on vendor route → 401/403; vendor token on admin route → 401/403; revoked vendor token → 401.

**PXG-057 ⬜ Vendor DTO & validation fixes** (M)
- Unify the field name to **`addressText`** in create, update and read DTOs (R8).
- `VendorUpdateDTO`: remove `isActive` from `required` (activation only via `PUT …/status`); decide PUT semantics: full replacement, `addressText` required in both create and update.
- `deliveryFeeType`: enum `NONE | FIXED` (extendable); rule: `FIXED` ⇒ `deliveryFeeAmount > 0`, `NONE` ⇒ amount absent or 0 (documented via description + enforced by validator; add `oneOf` if the generator copes).
- `phone`: normalise to E.164 and validate `^\+237[26]\d{8}$` (D14); duplicate phone → `409 PROXIGAZ-409-001`; duplicate email → `409-002`.
- `latitude`/`longitude`: keep range check and add a Cameroon bounding box guard (≈ lat 1.6–13.1, lon 8.4–16.2) → catches swapped coordinates (K11).
- Add `createdAt`, `updatedAt` to `VendorDTO`; add `email` (nullable) to `VendorDTO`; return `Location` header on 201.
- Tests: U validators; I migration/unique constraints; E create → read round-trip returns identical fields.

**PXG-058 ⬜ Vendor detail, session metadata, identity enrichment** (M)
- `GET /admin/vendors/{vendorId}` returning `VendorDetailDTO` = `VendorDTO` + `hasActiveSession`, `sessionCreatedAt`, `sessionLastUsedAt`, `isOpenNow` (when PXG-065 lands).
- `GET /admin/vendors`: add `sort` (`name`, `createdAt`, default `name,asc`), document that `name` is a case-insensitive *contains* filter.
- `GET /vendor/session` → `VendorSessionIdentityDTO` gains `name`, `isActive`; valid token for an inactive vendor returns **403 `PROXIGAZ-403-002`** (D11).
- Session guard updates `last_used_at` (throttled, e.g. once per minute).
- Responses carrying a token (`createVendor`, `rotateVendorSession`) send `Cache-Control: no-store`.
- Tests: E for each of the above.

**PXG-059 ⬜ Admin auth hardening** (M)
- `POST /admin/auth/login`: per email+IP lockout/backoff (e.g. 5 failures / 15 min → 429 `Retry-After`) via the `RateLimiter` port introduced here as an in-memory token bucket (D17); constant-time password check path for unknown emails.
- `AdminProfileDTO` gains `roles[]`; `AdminLoginDTO.password` gets `minLength`; token lifetime configurable; failed/successful logins logged without credentials.
- Tests: U bucket logic with `FakeClock`; E lockout then recovery.

**PXG-060 ⬜ Contract CI, SemVer, versioning** (S)
- Add an OpenAPI linter (e.g. Spectral) and a breaking-change check (e.g. `openapi-diff`) to CI; `info.version` set from the release tag; add the staging server URL.
- Done when: a PR that removes a required response field fails CI.

**PXG-061 ⬜ Admin audit log** (M)
- Migration `t_admin_audit_log` (Analysis §4.3). An `AuditLogPort` called by the vendor use cases (create, update, status, rotate, revoke) inside the business transaction. `GET /admin/audit-log?targetType&targetId` (paginated) for the operator.
- Tests: I rollback of the business change also rolls back the audit row; E no secret in metadata.

**PXG-062 ⬜ Documentation reconciliation & ADRs** (S)
- Record decisions D1–D20 as ADRs in `docs/adr/` (one short file each, status accepted/rejected); fix the Cadrage v1.4/v1.5 reference; update `status.md` with gates G1/G2 and their current state; mark PXG-005/009 notes.

**PXG-063 ⬜ API scenario scripts** (M)
- A versioned collection (Bruno/Postman/`.http` files + a runner script usable in CI against staging) with environments and a seed-reset step. Sprint 1 scenario: admin login → create vendor → keep one-time token → vendor `GET /vendor/session` → set brand + availability → public nearby → quote. Each later sprint extends it (PXG-022…, 036…, 048…).
- Done when: the runner executes the whole scenario green on staging.

### Epic 1.1 — Vendor Management (Admin) — functionally done

**US-1.1.1 — Onboard a vendor.** *As an admin, I want to create a vendor with its location and receive a one-time session token, so that I can hand the vendor access (e.g. over WhatsApp) without ever storing the secret.*

Acceptance criteria
- AC1 — Given valid details, when I POST `/admin/vendors`, then the vendor, its owning user and its session are created **atomically**, and I receive `vendorId` + `sessionToken` once.
- AC2 — Given the database, then only the token hash is stored; the raw token is never retrievable again.
- AC3 — Given a duplicate phone or email, then I receive 409 and nothing is created.
- AC4 — Given a location, then it is stored as `geography(Point,4326)` with longitude/latitude in the right order (verified by a known-distance test).

**US-1.1.2 — Operate the vendor lifecycle.** *As an admin, I want to update, activate/deactivate, list and filter vendors, so that only trustworthy vendors receive orders.*

Acceptance criteria
- AC1 — Given an inactive vendor, then it never appears in nearby results and cannot receive offers.
- AC2 — Given `page`, `size`, `isActive`, `name` filters, then the list returns the correct page and totals.
- AC3 — Given a vendor, when I rotate the session, then the old token stops working immediately and the new one is shown once; when I revoke, then no token works.

| ID | Status | Task | Details | Est. |
|---|---|---|---|---|
| PXG-012 | ✅ | Create / update vendor | `POST /admin/vendors`, `PUT /admin/vendors/{id}`; location as geography. *Contract fixes in PXG-057* | M |
| PXG-012A | ⛔ | (superseded) | Replaced by PXG-012B; kept only as history | — |
| PXG-012B | ✅ | Vendor aggregate + onboarding session | User root, Vendor aggregate with unique `user_id`, hashed one-time session; optional email nullable | L |
| PXG-012C | ❓ | Docker image & release automation | **Verify**: immutable image, staging deploy by digest, release notes, annotated SemVer tag; production promotion approval-gated *(only enforce from pilot)* | L |
| PXG-013 | ✅ | Activate / deactivate | `PUT /admin/vendors/{id}/status`. *Audit in PXG-061; nearby exclusion verified in PXG-020* | S |
| PXG-014 | ✅ | List / filter | Pagination (0-based), `isActive`, `name`. *Sort in PXG-058* | M |
| PXG-015 | ✅ | Session management & validation | Rotate (`POST …/session`), revoke (`DELETE`), `GET /vendor/session`. *Metadata in PXG-058* | M |

**PXG-012C verification checklist (❓ → ✅):** image tagged with SemVer + digest recorded · staging pulls the digest, not a mutable tag · release notes generated · annotated tag pushed · rollback command documented.

---

### Epic 1.2 — Vendor Brands & Availability

**US-1.2.1 — Maintain the catalogue (admin).** *As an admin, I want to define which brands and bottle sizes a vendor sells and at what price (with and without returning an empty bottle), so that clients see correct prices from day one.*

Acceptance criteria
- AC1 — Given a vendor, when I add a brand/size, then it stores `priceWithExchange` and `priceWithoutExchange` in integer FCFA, both > 0, unique per (vendor, brand, size).
- AC2 — Given a brand name that is not in the reference table, then creation is rejected with a validation error (no free-text brand names).
- AC3 — Given a price update, then past orders keep their old price (snapshots; verified in Sprint 2) and the change is audit-logged.

**US-1.2.2 — Update availability myself (vendor).** *As a vendor, I want to mark a brand available/unavailable until a time and switch my shop "open now" from my phone in one tap, so that I only receive orders I can serve.*

Acceptance criteria
- AC1 — Given my valid session, when I `PUT /vendor/open-status {isOpenNow:false}`, then I disappear from nearby results immediately.
- AC2 — Given a brand, when I set `isAvailable:true` with `availableUntil`, then it is sellable until that instant and unavailable afterwards **without any job running** (query-time).
- AC3 — Given I omit `availableUntil` when switching on, then the default is the end of the current day in Africa/Douala (documented), expressed in UTC.
- AC4 — Given another vendor's brand id, then I receive 404 (no information leak).

**US-1.2.3 — One rule for "available".** *As a developer, I want a single availability rule, so that nearby, order creation and fallback can never disagree.*

Acceptance criteria
- AC1 — Given every combination of `is_active`, `is_open_now`, `is_available` and `available_until` before/at/after `now`, then `isCurrentlyAvailable` returns true only when all four conditions hold (strictly `available_until > now`).
- AC2 — Given a `FakeClock`, then tests advance time across `available_until` and the result flips without touching the database.

| ID | Status | Task | Details | Est. |
|---|---|---|---|---|
| PXG-064 | ⬜ | Brand reference table | see below | M |
| PXG-016 | ⬜ | CRUD VendorBrand (admin) | see below | L |
| PXG-065 | ⬜ | Vendor self-service availability | see below | M |
| PXG-017 | ⬜ | Availability computation | see below | M |
| PXG-018 | ⛔ | Auto-expire job | Removed: availability is query-time truth; no job | — |
| PXG-019 | ➡️ | Reliability score | **Moved to Sprint 3, Epic 3.2** (needs order data) | — |

**PXG-064 ⬜ Brand reference table** (M)
- Migration `t_brands(id, name, is_active)` with unique `lower(name)`; seed from the brands collected in the manual test/vendor interviews; `GET /admin/brands`, `POST /admin/brands` (admin only).
- Tests: I unique constraint case-insensitivity; E create/list.

**PXG-016 ⬜ CRUD VendorBrand (admin)** (L)
- Migration `t_vendor_brands` (Analysis §4.3). Endpoints under `/admin/vendors/{vendorId}/brands`: `POST`, `GET` (list), `PUT /{brandLinkId}`, `DELETE /{brandLinkId}` (soft: sets unavailable and hides from nearby; keeps history).
- DTO: `brandId`, `sizeKg` (decimal, e.g. 6, 12.5), `priceWithExchange`, `priceWithoutExchange`, `isAvailable`, `availableUntil` (UTC, must be in the future when `isAvailable=true`). Soft warning (not error) in the response if `priceWithoutExchange ≤ priceWithExchange`.
- Audit entries via PXG-061; vendor must exist (404) — inactive vendors may still be edited.
- Tests: U price validation; I unique key; E 404/409/422 cases.

**PXG-065 ⬜ Vendor self-service availability & open status** (M)
- Add `is_open_now boolean default false` to `t_vendors` (D12) and expose it in `VendorDTO`.
- Vendor endpoints (vendorSessionAuth): `GET /vendor/brands`, `PUT /vendor/brands/{linkId}/availability {isAvailable, availableUntil?}`, `PUT /vendor/open-status {isOpenNow}`. A vendor can only touch its own rows (ownership check from the token, never from the body).
- Every change writes an audit/event row (`VENDOR_AVAILABILITY_CHANGED`) for later KPI work (how often vendors keep data fresh).
- Tests: E happy path; E cross-vendor access → 404; U default `availableUntil`; E inactive vendor → 403.

**PXG-017 ⬜ Availability computation** (M)
- Pure function in `domain/`: `isCurrentlyAvailable(vendor, brand, now)` = `vendor.isActive ∧ vendor.isOpenNow ∧ brand.isAvailable ∧ brand.availableUntil > now`. A SQL twin used by the nearby query (PXG-020) receives `:now` as a parameter.
- Tests: U truth table (16 flag combinations × before/equal/after boundary); I SQL twin returns the same answers as the domain function on the same fixtures (guards against divergence).

### Epic 1.3 — Geo Queries & Public Discovery

**US-1.3.1 — Find gas near me.** *As a client, I want to see open vendors near my position that have my bottle available, nearest first, with the price I would pay, so that I can choose quickly.*

Acceptance criteria
- AC1 — Given `lat`, `lng` and optional `brand`, `sizeKg`, `radiusM`, when I `GET /vendors/nearby`, then I receive only active, open vendors with at least one currently available matching brand, sorted by distance ascending, with `distanceMeters`.
- AC2 — Given a radius larger than `SEARCH_RADIUS_MAX_METERS`, then it is capped (and the applied radius is returned); no radius ⇒ `SEARCH_RADIUS_METERS`.
- AC3 — Given an unavailable brand, a closed vendor, an inactive vendor or a vendor outside the radius, then it is absent.
- AC4 — Given the response, then it does **not** include the vendor phone number (D16) nor internal ids beyond the vendor/brand-link ids needed to order.
- AC5 — Given 100 requests/min from one IP, then 429 is returned beyond the limit.
- AC6 — Given coordinates outside the Cameroon bounding box, then 400.

**US-1.3.2 — Know the total before ordering.** *As a client, I want to see the exact total (bottle with/without exchange + delivery fee, or 0 for pickup), so that there is no surprise.*

Acceptance criteria
- AC1 — Given a brand link, `hasEmptyBottle` and `isPickup`, when I `POST /orders/quote`, then I receive `unitPrice`, `deliveryFee`, `total`, `currency: XAF`, and `quotedAt`.
- AC2 — Given `isPickup=true`, then `deliveryFee = 0` regardless of the vendor's fee.
- AC3 — Given the brand is no longer available, then 409 `PROXIGAZ-409-013 BRAND_UNAVAILABLE`.
- AC4 — Quote performs no write.

| ID | Status | Task | Details | Est. |
|---|---|---|---|---|
| PXG-020 | ⬜ | Nearby query | see below | L |
| PXG-021 | ⬜ | Configurable radius | see below | S |
| PXG-066 | ⬜ | Public nearby endpoint | see below | M |
| PXG-067 | ⬜ | Quote endpoint | see below | M |

**PXG-020 ⬜ Nearby vendors query (`GeoQueryPort`)** (L)
- PostGIS adapter: `ST_DWithin(location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography, :radius)` + `ST_Distance` for ordering; join available brands using the SQL twin of PXG-017 with `:now`; filters `brand`, `sizeKg`; deterministic tie-break (distance, then vendor id). Note `ST_MakePoint(lng, lat)` argument order.
- Also an in-memory adapter for unit tests of use cases.
- Tests: I fixtures with two points a known ~1 km apart (guards lat/lng swap); I boundary at exact radius; I flag combinations; I uses GIST (check with `EXPLAIN` in a test on 1 000 generated vendors).

**PXG-021 ⬜ Configurable search radius** (S)
- Read `SEARCH_RADIUS_METERS` and `SEARCH_RADIUS_MAX_METERS` from config (D20); read-only `GET /admin/settings` returns effective values (no secrets).
- Tests: U capping logic; E changing config changes results.

**PXG-066 ⬜ Public nearby endpoint** (M)
- `GET /vendors/nearby` (`security: []`) → `NearbyVendorDTO[]`: `vendorId`, `name`, `addressText`, `isStation`, `distanceMeters`, `deliveryFeeType`, `deliveryFeeAmount`, `brands[{brandLinkId, brandName, sizeKg, priceWithExchange, priceWithoutExchange}]`, `ratingAverage`/`ratingCount` (null/0 until Sprint 3). Response limited to 30 vendors; `Cache-Control: no-store`.
- Rate limiter (30 req/min/IP) via `RateLimiter`; validation of coordinates.
- Tests: E all ACs above; E response contains no phone field (schema assertion).

**PXG-067 ⬜ Quote endpoint** (M)
- `POST /orders/quote` (public) with `brandLinkId`, `hasEmptyBottle`, `isPickup`. Pure `PriceCalculator` in `domain/` (Money in FCFA), reused by order creation (PXG-022) so both always agree.
- Tests: U price matrix (exchange × pickup × fee types); E unavailable brand → 409.

**Sprint 1 exit criteria**
- Contract lint green, no template leftovers, error model everywhere, audit log in place.
- Admin manages the full vendor + brand lifecycle; vendor maintains availability and open status.
- Public nearby + quote work with PostGIS; availability rule unit-tested and SQL/domain twins consistent.
- Scenario script (PXG-063) passes on staging with a tagged image; **contract C1 published**.

---

## Sprint 2a — Order Flow, vertical slice (no push, no fallback)
**Gate:** **G1 must be satisfied** before PXG-022.
**Goal:** A client creates an order; the vendor sees the offer (by polling), accepts or refuses; the client tracks the result with a token.
**Duration estimate:** ~1.5–2 weeks. **Milestone:** contract **C2**.

### Epic 2.1 — Order Creation & Tracking

**US-2.1.1 — Place an order.** *As a client, I want to order a bottle from the vendor I chose with the total I saw, so that I get my gas without negotiating.*

Acceptance criteria
- AC1 — Given an available brand, a valid phone, and (for delivery) a location and landmark, when I `POST /orders` with the `quotedTotal` I saw, then I receive 201 with `trackingToken` (once), `publicRef`, `status: EN_ATTENTE`, `total`, and the order has exactly one `PROPOSED` offer expiring at `now + OFFER_TTL_SECONDS`.
- AC2 — Given the vendor changed the price since my quote, then I receive 409 `PRICE_CHANGED` with the new quote and **nothing is created**.
- AC3 — Given the brand is unavailable/closed/inactive at creation time, then 409 `BRAND_UNAVAILABLE`.
- AC4 — Given I already have 2 active orders, then 409 `MAX_ACTIVE_ORDERS`; given 5 requests in 10 s for the same phone, then I cannot create more than 2 even under concurrency.
- AC5 — Given a delivery order without `clientLandmark` or location, then 400 with field errors; pickup orders need neither.
- AC6 — Given creation, then `order_events` contains `ORDER_CREATED` and `OFFER_CREATED` with actor CLIENT/SYSTEM.
- AC7 — Given the code, then a 4-digit `confirmationCode` exists on the order and is **not** present in any vendor-facing response.

**US-2.1.2 — Track my order.** *As a client, I want to follow my order with a private link, so that I know whether a vendor accepted.*

Acceptance criteria
- AC1 — Given a valid token, when I `GET /orders/track/{token}`, then I see `publicRef`, `status`, `createdAt`, `isPickup`, `total`, `attemptNumber`/`maxAttempts`, `canCancel`, and — once confirmed — vendor `name`, `phone`, `addressText` and the `confirmationCode`.
- AC2 — Given an unknown token, then 404 with the same body and similar latency as any other not-found (no enumeration); tokens cannot be guessed (≥ 128 bits) nor derived from ids.
- AC3 — The response never contains my phone, my coordinates, or other vendors' data; `Cache-Control: no-store`; 60 req/min/IP limit.

**US-2.1.3 — Cancel before it starts.** *As a client, I want to cancel an order that has not started, so that I am not stuck with a vendor I no longer want.*

Acceptance criteria
- AC1 — Given status `EN_ATTENTE` or `CONFIRMEE`, when I cancel, then status becomes `ANNULEE`, the open offer becomes `CANCELLED`, my active slot is freed, and an event is written.
- AC2 — Given status `EN_COURS` or later, then 409.
- AC3 — Given a second cancel call, then the result is idempotent (200/204 with the same final state).

| ID | Status | Task | Details | Est. |
|---|---|---|---|---|
| PXG-068 | ⬜ | Order/offer/event migrations | see below | M |
| PXG-069 | ⬜ | Extend state machines | see below | M |
| PXG-022 | ⬜ | Order creation use case | see below | L |
| PXG-023 | ⬜ | Max 2 active orders + rate limiting | see below | L |
| PXG-024 | ⬜ | Price snapshot | see below | M |
| PXG-025 | ⬜ | Tracking by token | see below | M |
| PXG-070 | ⬜ | Client cancel | see below | M |

**PXG-068 ⬜ Order, offer, event migrations** (M)
- Create `t_orders`, `t_order_offers`, `t_order_events` exactly as Analysis §4.3 (including `public_ref`, `tracking_token_hash`, `active_slot`, partial unique indexes, `version`). Status and event types as constrained text (CHECK) for easy evolution.
- Rollback migration tested; seeds include no orders.
- Tests: I the partial unique indexes reject a second `PROPOSED` offer per order and a third active order per phone.

**PXG-069 ⬜ Extend state machines** (M)
- Update `OrderStatus` to `EN_ATTENTE, CONFIRMEE, EN_COURS, LIVREE, LIVREE_VERIFIEE, EXPIREE, ANNULEE, ECHOUEE` and the transition table of Analysis §4.1; add `OfferStatus` machine; `TerminalReason` enum (`ATTEMPTS_EXHAUSTED`, `NO_ELIGIBLE_VENDOR`, `PICKUP_NO_FALLBACK`, `CLIENT_CANCELLED`, `ADMIN_CANCELLED`, `VENDOR_WITHDREW`, `DELIVERY_TIMEOUT`).
- Illegal transitions throw a domain error mapped to 409.
- Tests: U every allowed transition passes, every other pair of statuses is rejected (table-driven).

**PXG-022 ⬜ Order creation use case** (L)
- Input: `clientPhone`, `brandLinkId`, `hasEmptyBottle`, `isPickup`, `clientLocation?`, `clientLandmark?` (3–200 chars, required for delivery), `quotedTotal`.
- One transaction: normalise phone → (hook for block list, active from PXG-042) → rate limit → acquire active slot (PXG-023) → load brand + vendor and verify availability at `clock.now()` → recompute price with `PriceCalculator` (PXG-067) and compare with `quotedTotal` → insert order (`EN_ATTENTE`, `confirmation_code` from a CSPRNG, `public_ref` Crockford base32 ×6–8, `tracking_token` random 128+ bits stored as SHA-256) → insert offer #1 (snapshots, `expires_at = now + TTL`) → insert events → commit → publish an `OfferCreated` application event **after commit** (consumed by PXG-031 in 2b).
- Response `OrderCreatedDTO`: `publicRef`, `trackingToken`, `status`, `total`, `isPickup`. Optional `Idempotency-Key` accepted from PXG-044.
- Tests: U use case with fakes; I transaction atomicity (failure after order insert leaves nothing); E ACs 1–7; E price drift → 409.

**PXG-023 ⬜ Max 2 active orders + basic rate limiting** (L)
- `RateLimiter` port + in-memory token-bucket adapter (D17); policies: `POST /orders` 10/h per IP and 5/h per phone. Trust `X-Forwarded-For` only from the configured proxy.
- Active-slot mechanism: `active_slot ∈ {1,2}` with partial unique `(client_phone, active_slot) WHERE active_slot IS NOT NULL`; creation inserts slot 1, on unique violation slot 2, on second violation → 409 `MAX_ACTIVE_ORDERS`. The slot is cleared whenever the order leaves an active status (domain hook in the transition code).
- **No device fingerprinting** in the MVP (weak and privacy-costly).
- Tests: C 10 parallel creations for one phone → exactly 2 succeed; U bucket refill with `FakeClock`; E 429 + `Retry-After`; I slot freed on `ANNULEE`, `EXPIREE`, `LIVREE`.

**PXG-024 ⬜ Price snapshot** (M)
- Store `unit_price`, `delivery_fee`, `quoted_total` on the order and the per-offer `*_snapshot` values; `final_total` set at acceptance (D5). Prices are **never** recomputed from the live brand after creation.
- Tests: I change the brand price after creation → order/offer unchanged; U `final_total ≤ quoted_total` guard.

**PXG-025 ⬜ Tracking by token** (M)
- `GET /orders/track/{token}` per US-2.1.2: hash the incoming token, constant-time lookup, uniform 404; vendor block and code visible only in `CONFIRMEE`/`EN_COURS`; computed flags `canCancel`, `canConfirmReception` (S3), `canReview` (S3).
- Tests: E payload schema has no phone/coordinates; E unknown token → identical 404; I lookup uses the unique hash index.

**PXG-070 ⬜ Client cancel** (M)
- `POST /orders/track/{token}/cancel` (optional `reason`); transitions per Analysis §4.1; open offer → `CANCELLED`; frees the slot; event `ORDER_CANCELLED` (actor CLIENT).
- Tests: E all ACs; C cancel vs vendor accept race (one winner, consistent state).

### Epic 2.2 — Vendor Order Actions

**US-2.2.1 — See what I must answer.** *As a vendor, I want to see my pending offers with what I need to decide, so that I can accept quickly from my phone.*

Acceptance criteria
- AC1 — Given my token, when I `GET /vendor/offers`, then I see only my `PROPOSED`, non-expired offers (by `expires_at > now`), oldest first, each with `offerId`, `publicRef`, brand/size, `hasEmptyBottle`, `isPickup`, `total`, `clientLandmark`, approximate `distanceMeters`, `expiresAt`, `secondsLeft`.
- AC2 — Given another vendor's offer, it never appears, and `GET /vendor/orders/{id}` returns 404 for it.
- AC3 — Before I accept, the client phone and exact coordinates are **not** present (D16).
- AC4 — `GET /vendor/orders` lists my orders (today by default, `from`/`to` filters, paginated) so I can review history.

**US-2.2.2 — Accept an order.** *As a vendor, I want to accept an offer, so that the client knows I will serve them.*

Acceptance criteria
- AC1 — Given my live `PROPOSED` offer, when I accept, then the offer is `ACCEPTED`, the order `CONFIRMEE`, `confirmed_vendor_id` and `final_total` set, events written, all in one transaction.
- AC2 — Given the offer expired a moment ago, was already answered, or the order was cancelled, then 409 `OFFER_NOT_ACTIONABLE` and no state changes.
- AC3 — Given two simultaneous accept calls, then exactly one succeeds.
- AC4 — After acceptance I can read the client phone and coordinates via `GET /vendor/orders/{id}`.
- AC5 — The tracking endpoint now shows my vendor details and the confirmation code to the client.

**US-2.2.3 — Refuse an order.** *As a vendor, I want to refuse an offer with a reason, so that the order is not blocked on me.*

Acceptance criteria
- AC1 — Given my live offer, when I refuse with `reason ∈ {OUT_OF_STOCK, TOO_FAR, CLOSED, SUSPECTED_FAKE, OTHER}`, then the offer is `REFUSED` with `responded_at`, and the dispatch policy runs.
- AC2 — In Sprint 2a the policy is "terminate": the order becomes `EXPIREE` (`ATTEMPTS_EXHAUSTED` with `OFFER_MAX_ATTEMPTS` forced to 1 for this sprint); Sprint 2b replaces it by fallback without changing this endpoint.
- AC3 — `SUSPECTED_FAKE` also creates an order report once Sprint 3 lands (hook present now, no-op).

| ID | Status | Task | Details | Est. |
|---|---|---|---|---|
| PXG-026 | ⬜ | Vendor session guard | see below | M |
| PXG-027 | ⬜ | Pending offers + order history | see below | M |
| PXG-028 | ⬜ | Accept offer | see below | L |
| PXG-029 | ⬜ | Refuse offer | see below | M |

**PXG-026 ⬜ Vendor authentication guard** (M)
- Single guard for `/vendor/**`: parse the opaque token, look up by prefix, compare SHA-256 in constant time, require non-revoked session and **active** vendor (else `403-002`); inject `vendorId` into the request context; failed attempts rate-limited per IP (10/min). The resolved `vendorId` is the *only* source of ownership in use cases.
- Tests: E revoked / rotated / unknown / inactive; E IDOR matrix over every `/vendor/**` route (vendor A ↔ resources of vendor B).

**PXG-027 ⬜ Pending offers & vendor order history** (M)
- `GET /vendor/offers` (live filter by `:now`), `GET /vendor/orders`, `GET /vendor/orders/{id}`. Phone and coordinates revealed only when `confirmed_vendor_id = me`.
- Tests: E filtering/ownership/expiry boundary with `FakeClock`; E data minimisation before/after acceptance.

**PXG-028 ⬜ Accept offer** (L)
- `POST /vendor/offers/{offerId}/accept`: conditional update `UPDATE t_order_offers SET status='ACCEPTED', responded_at=:now WHERE id=:id AND vendor_id=:vendor AND status='PROPOSED' AND expires_at > :now`; if 0 rows → 409 `409-012 OFFER_NOT_ACTIONABLE`. Then `UPDATE t_orders … WHERE id=:o AND status='EN_ATTENTE'` (also optimistic `version`), set `CONFIRMEE`, `confirmed_vendor_id`, `final_total`, `confirmed_at`; events `OFFER_ACCEPTED`, `ORDER_CONFIRMED`. **Client is not pushed** (D6): visibility comes via tracking.
- Tests: C two concurrent accepts, accept vs cancel, accept at `expires_at − 1 ms` vs `+ 1 ms` (FakeClock); E happy path; I one transaction rolls back on failure.

**PXG-029 ⬜ Refuse offer** (M)
- `POST /vendor/offers/{offerId}/refuse {reason}` with the same conditional-update pattern. Calls `OfferDispatchPolicy.onOfferClosed(order)`; Sprint 2a implementation terminates the order (`EXPIREE`), Sprint 2b injects the fallback implementation (PXG-034).
- Tests: E all refusal reasons; U policy invoked once; E refusing twice → 409.

**Sprint 2a exit criteria**
- Scenario script: client quote → create order → vendor lists offer → accept → client tracking shows `CONFIRMEE` + vendor + code.
- Cancel works; double-accept and double-create under concurrency are safe.
- IDOR matrix passes. **Contract C2 published**; tagged staging release.

---

## Sprint 2b — Notifications, Expiry & Fallback
**Gate:** **G2 must be recorded** before PXG-030.
**Goal:** Vendors are notified of new offers, silent offers expire safely, fallback finds the next vendor, and operators can escalate by phone.
**Duration estimate:** ~1.5–2 weeks. **Milestone:** contract **C3**.

### Epic 2.3 — Notifications & Escalation

**US-2.3.1 — Be alerted of a new offer.** *As a vendor, I want my phone to alert me when an order arrives, so that I do not have to keep the app open.*

Acceptance criteria
- AC1 — Given a registered push subscription, when an offer is created, then a push with `{type:"NEW_OFFER", offerId, publicRef, expiresAt}` is sent within seconds — **no client data in the payload**.
- AC2 — Given the push provider returns 404/410, then the subscription is disabled; given any push failure, then the order flow is unaffected and the failure is recorded on the offer (`notify_status`).
- AC3 — Given no subscription, then nothing breaks and the offer is still visible via polling and on the escalation list.
- AC4 — Given I register/unregister a subscription via `POST/DELETE /vendor/push-subscriptions`, then only my own subscriptions are affected and the same endpoint registered twice is idempotent.

**US-2.3.2 — Escalate silent offers.** *As an operator, I want a list of offers that have waited more than 5 minutes, with the vendor phone number, so that I can call the vendor.*

Acceptance criteria
- AC1 — Given a `PROPOSED` offer older than `ESCALATION_AFTER_SECONDS`, when I `GET /admin/orders/needs-attention`, then it appears with `publicRef`, vendor name/phone, age, `notify_status`, and reason `OFFER_SILENT`.
- AC2 — Given I called the vendor, when I `POST /admin/offers/{id}/escalation-ack`, then an `ESCALATION_ACK` event is recorded (who, when) and the item shows as acknowledged.
- AC3 — Given the list, then it also carries other attention reasons as they appear later (`CODE_LOCKED`, `EXPIREE_PICKUP`, `STALE_EN_COURS`).

| ID | Status | Task | Details | Est. |
|---|---|---|---|---|
| PXG-071 | ⬜ | Push subscription endpoints | see below | M |
| PXG-030 | ⬜ | Web Push adapter | see below | L |
| PXG-031 | ⬜ | Notify vendor on new offer | see below | M |
| PXG-032 | ⬜ | Manual escalation list | see below | M |

**PXG-071 ⬜ Push subscription endpoints** (M)
- Migration `t_push_subscriptions`. `POST /vendor/push-subscriptions {endpoint, keys:{p256dh, auth}}` (upsert by endpoint), `DELETE /vendor/push-subscriptions` (by endpoint in body). Expose the VAPID **public** key at `GET /vendor/push-config`.
- Tests: E idempotent register; E cannot delete another vendor's subscription; I unique endpoint.

**PXG-030 ⬜ Web Push adapter** (L)
- `NotificationChannel` implementation with VAPID (keys in secrets, public key exposed); `urgency: high`, TTL = remaining offer time; payload as per AC1; 404/410 ⇒ disable subscription; timeouts (e.g. 5 s) and bounded retries (max 2) so a slow provider cannot stall requests. Behind the port, so a `LoggingNotificationChannel` remains for tests and a future SMS/WhatsApp channel can be added.
- Respects **G2**: if the real-phone test failed, ship the channel disabled by config and rely on PXG-032.
- Tests: U payload builder (no PII); I fake push server returning 201/404/410/500/timeout.

**PXG-031 ⬜ Notify vendor on new offer** (M)
- Handler for `OfferCreated` (published after commit in PXG-022 and by fallback). Sends to all active subscriptions of the vendor, records `notified_at` and `notify_status` (`SENT`, `FAILED`, `NO_SUBSCRIPTION`) on the offer.
- Failures are caught and logged; they never roll back or fail the originating transaction.
- Tests: I failure isolation; E offer visible via polling when push fails.

**PXG-032 ⬜ Manual escalation support** (M)
- `GET /admin/orders/needs-attention` (paginated, oldest first) and `POST /admin/offers/{id}/escalation-ack`. Query-time evaluation with `:now`; item reasons are an enum so later sprints just add producers.
- Tests: E boundary at exactly 300 s; E ack recorded once (idempotent); E vendor phone present for admin only.

### Epic 2.4 — Offer Expiry & Fallback

**US-2.4.1 — Never leave a client hanging.** *As a client, I want my order to move on automatically if a vendor does not answer or refuses, so that I get a vendor without calling anyone.*

Acceptance criteria
- AC1 — Given an offer past `expires_at`, then it becomes `EXPIRED` (event `OFFER_EXPIRED`) even if the worker was down for a while (catch-up), and it can no longer be accepted (guard in PXG-028 is authoritative).
- AC2 — Given a refused or expired offer on a **delivery** order with `attempt_count < OFFER_MAX_ATTEMPTS`, then the next eligible vendor receives offer #n+1 (D4 eligibility and ranking) and the vendor is notified.
- AC3 — Given D3, then only vendors whose `total ≤ quoted_total` are candidates; the number excluded by price is stored in the event metadata (`FALLBACK_PRICE_EXCLUDED`).
- AC4 — Given attempts exhausted or no eligible vendor, then the order becomes `EXPIREE` with the right terminal reason and the active slot is freed; the tracking endpoint shows it.
- AC5 — Given a **pickup** order, then no automatic fallback happens (D4b): it becomes `EXPIREE (PICKUP_NO_FALLBACK)` and appears on the needs-attention list.
- AC6 — Given a vendor already offered on this order, then it is never offered again.
- AC7 — Worst-case time to a final answer is ≤ `OFFER_MAX_ATTEMPTS × OFFER_TTL_SECONDS` (30 min with defaults) and is covered by a test.

**US-2.4.2 — Operate the worker safely.** *As an operator, I want to know the expiry worker is alive, so that I can intervene if it stops.*

Acceptance criteria
- AC1 — Given the worker, then it records a heartbeat each run; `GET /health/ready` (and the admin settings view) expose `lastExpiryRunAt`, and it turns unhealthy if older than 2 minutes.
- AC2 — Given two app instances (or two overlapping runs), then no offer is processed twice.

| ID | Status | Task | Details | Est. |
|---|---|---|---|---|
| PXG-033 | ⬜ | Fallback rule decision (ADR) | see below | S |
| PXG-072 | ⬜ | Offer expiry mechanism | see below | L |
| PXG-034 | ⬜ | Fallback implementation | see below | L |
| PXG-073 | ⬜ | Concurrency & race suite | see below | L |
| PXG-074 | ⬜ | Worker operations | see below | S |

**PXG-033 ⬜ Fallback rule decision** (S)
- Ratify and document D2, D3, D4, D4b as ADRs. If you reject D3, the alternative (Option A: client explicitly accepts a new total) additionally requires a client notification channel and a `PENDING_CLIENT_APPROVAL` offer state — **re-estimate before choosing it**.
- Done when: ADRs accepted and the configuration defaults are committed.

**PXG-072 ⬜ Offer expiry mechanism** (L)
- Two layers: **(1) query-time** — reads and `accept` already require `expires_at > :now`; **(2) worker** every 15–30 s: `SELECT … WHERE status='PROPOSED' AND expires_at <= :now ORDER BY expires_at LIMIT 50 FOR UPDATE SKIP LOCKED`, each offer handled in its own transaction (`EXPIRED` → event → `OfferDispatchPolicy.onOfferClosed`). Idempotent and catch-up capable.
- Free-tier hosting note: the process hosting the worker must stay awake (PXG-074); otherwise only layer 1 protects correctness.
- Tests: I overdue offers processed after a simulated outage; C two workers → each offer once; U `FakeClock` boundary.

**PXG-034 ⬜ Fallback implementation** (L)
- `OfferDispatchPolicy` implementation per D3/D4/D4b. Candidate query via `GeoQueryPort.eligibleForFallback(order, excludedVendorIds, now)` returning vendors sorted by distance, then reliability (NULL = 50), then total, then id. Creates offer `attempt_no+1` with a fresh `expires_at`, snapshots, events `OFFER_CREATED`/`FALLBACK_PRICE_EXCLUDED`, publishes `OfferCreated`. Replaces the Sprint 2a "terminate" policy and sets `OFFER_MAX_ATTEMPTS` back to 3.
- Tests: U ranking and exclusion (table-driven); I price rule; E three-attempt scenario; E pickup behaviour; E no-candidate case; U worst-case duration.

**PXG-073 ⬜ Concurrency & race suite** (L)
- Dedicated C tests, run in CI against real PostgreSQL: accept vs expire, accept vs accept, refuse vs expire, cancel vs accept, worker vs worker, create-order burst per phone, fallback picks distinct vendors under parallel refusals. Each asserts a single winner and a consistent order/offer/event trail.

**PXG-074 ⬜ Worker operations** (S)
- Heartbeat table/row or in-memory + DB timestamp, `lastExpiryRunAt` in `GET /health/ready`; configuration for run interval and batch size; deployment note in the runbook: single always-on instance for the pilot.

**Sprint 2b exit criteria**
- Full slice with push, escalation, expiry and fallback; all PXG-073 tests green.
- Scenario script extended: refusal → fallback to vendor 2 → accept; silent offer → escalation list → ack.
- **Contract C3 published**; tagged staging release.

---

## Sprint 3 — Delivery, Confirmation Code, Reviews, Reliability & Anti-abuse
**Goal:** Complete the lifecycle to `LIVREE` / `LIVREE_VERIFIEE`, reviews, abuse controls and pilot KPIs.
**Duration estimate:** ~2 weeks. **Milestone:** contract **C4**.

### Epic 3.1 — Delivery & Confirmation

**US-3.1.1 — Deliver and prove it.** *As a vendor, I want to start the delivery and close it with the client's 4-digit code, so that the platform knows the bottle was handed over.*

Acceptance criteria
- AC1 — Given `CONFIRMEE` and I am the confirmed vendor, when I `POST /vendor/orders/{id}/start`, then status is `EN_COURS`, `started_at` set, event written; anyone else gets 404.
- AC2 — Given `EN_COURS`, when I `POST …/complete {code}` with the right code, then status is `LIVREE`, `delivered_at` set, the active slot is freed.
- AC3 — Given a wrong code, then 422 `INVALID_CODE` with `attemptsRemaining`; after the **3rd** wrong code, 423 `CODE_LOCKED`, `code_locked_at` set, the order stays `EN_COURS` and appears on the needs-attention list (D8).
- AC4 — Given the locked state, when an admin resolves it (PXG-049), then it moves to `LIVREE` or `ECHOUEE` with an audited reason.
- AC5 — Attempts are counted atomically (two parallel wrong submissions count as two).

**US-3.1.2 — Confirm I received it.** *As a client, I want to confirm reception, so that I can leave a review and the vendor gets credit.*

Acceptance criteria
- AC1 — Given `LIVREE`, when I `POST /orders/track/{token}/confirm-reception`, then status is `LIVREE_VERIFIEE` with `client_confirmed_at`.
- AC2 — Given any other status, then 409.
- AC3 — Given I confirm twice, the call is idempotent.

**US-3.1.3 — Nothing stays stuck.** *As an operator, I want stale orders to be flagged or closed automatically, so that dashboards and slots stay truthful.*

Acceptance criteria
- AC1 — Given `EN_COURS` for more than `STALE_EN_COURS_HOURS` without a correct code and not locked, then the order becomes `ECHOUEE (DELIVERY_TIMEOUT)` and the slot is freed.
- AC2 — Given `CONFIRMEE` not started after the same threshold, then it appears as `STALE_CONFIRMEE` on the needs-attention list (no auto change).
- AC3 — Given `LIVREE` without confirmation after `AUTO_CLOSE_LIVREE_HOURS`, then `auto_closed_at` is set: it still counts as *Livrée* in KPIs but the review stays locked.

**US-3.1.4 — I cannot serve this order anymore.** *As a vendor, I want to withdraw from a confirmed order, so that the client and the operator know early.*

Acceptance criteria
- AC1 — Given `CONFIRMEE`/`EN_COURS` and I am the vendor, when I `POST …/withdraw {reason}`, then the order is `ECHOUEE (VENDOR_WITHDREW)`, the offer is marked, the order appears on the needs-attention list for re-dispatch, and my reliability is penalised (PXG-019).

| ID | Status | Task | Details | Est. |
|---|---|---|---|---|
| PXG-035 | ⬜ | Start delivery / pickup | see below | M |
| PXG-036 | ⬜ | Submit confirmation code | see below | L |
| PXG-037 | ⬜ | Client confirmation | see below | M |
| PXG-038 | ⬜ | Stale-order sweeper | see below | M |
| PXG-075 | ⬜ | Vendor withdraw after accept | see below | M |

**PXG-035 ⬜ Start delivery / pickup** (M)
- `POST /vendor/orders/{id}/start`; for pickup orders the semantics are "ready for pickup" (documented), same transition. Conditional update on status + `confirmed_vendor_id`.
- Tests: E happy path; E wrong vendor → 404; C double start.

**PXG-036 ⬜ Submit confirmation code** (L)
- `POST /vendor/orders/{id}/complete {code}`; constant-time comparison; attempts via `UPDATE … SET code_attempts = code_attempts + 1 … RETURNING` inside the same transaction; at 3 → `code_locked_at`; `INVALID_CODE` response never reveals digits. Adds `422 INVALID_CODE` and `423-001 CODE_LOCKED` to the registry. Optional `Idempotency-Key` (PXG-044).
- Tests: U attempt counting; C parallel wrong codes; E success, failure, lock; I slot freed on success.

**PXG-037 ⬜ Client confirmation** (M)
- `POST /orders/track/{token}/confirm-reception`; tracking flags `canConfirmReception` and `canReview` computed.
- Tests: E all ACs; U idempotency.

**PXG-038 ⬜ Stale-order sweeper (replaces "24 h auto-stats")** (M)
- Job (every 5–10 min, `SKIP LOCKED`) implementing US-3.1.3; uses `Clock`; each action writes events with actor SYSTEM.
- Tests: U/I with `FakeClock` for each threshold; C worker-vs-vendor race on completion.

**PXG-075 ⬜ Vendor withdraw after accept** (M)
- `POST /vendor/orders/{id}/withdraw {reason}`; transition per D9; needs-attention reason `VENDOR_WITHDREW`; reliability event emitted.
- Tests: E happy path; E not allowed after `LIVREE`; I slot freed.

### Epic 3.2 — Reviews & Vendor Reliability

**US-3.2.1 — Rate my vendor.** *As a client, I want to rate a delivery after confirming it, so that others can choose better vendors.*

Acceptance criteria
- AC1 — Given `LIVREE_VERIFIEE`, when I `POST /orders/track/{token}/review {rating 1–5, comment?≤500}`, then it is stored; second attempt → 409.
- AC2 — Given any status before `LIVREE_VERIFIEE`, then 409.
- AC3 — Given nearby results, then each vendor shows `ratingAverage` (1 decimal) and `ratingCount`; comments are **not** public.
- AC4 — Comments are stored as plain text (no markup), length-limited.

**US-3.2.2 — Prefer reliable vendors.** *As the platform, I want a reliability score per vendor, so that fallback and the admin can favour dependable vendors.*

Acceptance criteria
- AC1 — Given a vendor with ≥ 5 offers in the last 30 days, then `reliability_score` follows D18 and is stored with `reliability_updated_at`; fewer than 5 ⇒ `NULL`.
- AC2 — Given an offer closes (accepted/refused/expired) or an order is withdrawn, then the score is updated (event-driven) and recomputed nightly for the rolling window.
- AC3 — Given the admin vendor list, then the score is visible; it is **not** in public responses.
- AC4 — Fallback ranking (PXG-034) uses it with NULL = 50.

| ID | Status | Task | Details | Est. |
|---|---|---|---|---|
| PXG-039 | ⬜ | Create review | `t_reviews` migration; endpoint above; unique `order_id`; rate limited; comment sanitised | M |
| PXG-040 | ⬜ | Aggregate rating | Average + count via query (or denormalised columns refreshed on insert); included in `NearbyVendorDTO`; tests with several reviews, none, one | M |
| PXG-019 | ⬜ | Vendor reliability score (moved from Sprint 1) | Formula D18; counters from `t_order_offers` + withdrawals; event-driven + nightly job; configurable weights; tests U formula table, I rolling window with `FakeClock` | L |

### Epic 3.3 — Anti-abuse & Pilot KPIs

**US-3.3.1 — Report a fake order.** *As a vendor, I want to flag a suspicious order in one click, so that the operator can act and the fake-order rate is measured.*

Acceptance criteria
- AC1 — Given an order I received an offer for, when I `POST /vendor/orders/{id}/report {reason, note?}`, then an `OPEN` report is stored once per (order, vendor); another vendor's order → 404.
- AC2 — Given a refusal with reason `SUSPECTED_FAKE`, then a report is created automatically.
- AC3 — Given the admin, when I `GET /admin/reports` and `PUT /admin/reports/{id} {status: CONFIRMED|DISMISSED}`, then the status is stored with reviewer and time.
- AC4 — Given two confirmed reports for the same phone, then the admin list flags it as "suggested block" (no automatic block).

**US-3.3.2 — Block abusers.** *As an admin, I want to block a phone number, so that it cannot create orders.*

Acceptance criteria
- AC1 — Given a blocked phone, when it calls `POST /orders`, then 403 with a neutral message and no order is created.
- AC2 — Given the admin, I can list, add (with reason) and remove blocks; all changes are audit-logged.
- AC3 — A block does not cancel existing orders (admin can cancel via PXG-049).

**US-3.3.3 — Measure the pilot.** *As the product owner, I want the pilot KPIs computed exactly as defined, so that I can decide Go/No-Go and trigger the OTP when needed.*

Acceptance criteria
- AC1 — Given `GET /admin/metrics/pilot?from&to&vendorId?`, then I get each KPI of Analysis §6 with numerator, denominator and value, plus a per-vendor breakdown.
- AC2 — Given the fake-order rate over a rolling 7 days with ≥ 30 orders exceeding 10 %, then `otpTrigger.reached = true`.
- AC3 — Given a seeded dataset with known outcomes, then every KPI equals the hand-computed value.
- AC4 — Computation uses `order_events`, `order_offers`, `order_reports`, `t_reviews` only (no hidden state).

| ID | Status | Task | Details | Est. |
|---|---|---|---|---|
| PXG-041 | ⬜ | Report fake order | `t_order_reports` migration; vendor endpoint; auto-report on `SUSPECTED_FAKE`; tests E ownership/uniqueness | M |
| PXG-076 | ⬜ | Admin report triage | `GET /admin/reports`, `PUT /admin/reports/{id}`, "suggested block" flag; audit | M |
| PXG-042 | ⬜ | Phone block list | `t_blocked_phones`; admin CRUD; enforcement in PXG-022 (hook activated); tests E blocked create → 403, unblock restores | M |
| PXG-043 | ⬜ | Pilot KPI endpoint | SQL/views per Analysis §6; `otpTrigger`; tests on a seeded scenario dataset with hand-computed expectations | L |

**Sprint 3 exit criteria**
- Happy path to `LIVREE_VERIFIEE` + review; failure paths (wrong code/lock, withdrawal, timeout, fake report, blocked phone) tested.
- KPI endpoint matches hand-computed numbers; reliability feeds fallback ranking.
- Scenario script extended end-to-end. **Contract C4 published**; tagged staging release.

---

## Sprint 4 — Hardening, Admin Operations & Pilot Readiness
**Goal:** Operable by one person for the pilot neighbourhood (15–20 vendors): safe under abuse and concurrency, observable, documented.
**Duration estimate:** ~2 weeks. **Milestone:** contract **C5**.

### Epic 4.1 — Robustness & Observability

**US-4.1.1 — Safe under retries and abuse.** *As an operator, I want double-submits, retries and abusive traffic to be harmless, so that the pilot data stays clean.*

Acceptance criteria
- AC1 — Given `POST /orders`, `…/accept`, `…/complete` with the same `Idempotency-Key` and payload, then the first response is replayed and no second effect occurs; same key with a different payload → 422.
- AC2 — Given invalid input on any public endpoint (bad phone, oversized body, unknown properties), then 400 with field-level errors and nothing persisted.
- AC3 — Given abusive traffic, then 429 with `Retry-After`; limits are configurable and counters visible in metrics.
- AC4 — Given a load test (PXG-078), then targets are met without errors.

**US-4.1.2 — Diagnose in minutes.** *As the developer on call, I want structured logs, correlation IDs, metrics and health checks, so that I can find a failed push or stuck worker quickly.*

Acceptance criteria
- AC1 — Every request has a correlation ID returned in `X-Request-Id` and in problem responses; logs are JSON.
- AC2 — Logs never contain raw tokens, passwords, full phone numbers (masked, e.g. `+237 6** *** 123`) or client coordinates.
- AC3 — Metrics exist for orders created, offers by status, push sent/failed, worker lag, 429 count, p95 latency; health endpoints report DB and worker status.
- AC4 — A database backup can be restored on a clean instance (documented, rehearsed once).

| ID | Status | Task | Details | Est. |
|---|---|---|---|---|
| PXG-044 | ⬜ | Idempotency keys (reduced scope) | `t_idempotency_keys`; `Idempotency-Key` on create-order, accept, complete; 24 h TTL; request-hash check; tests C same key in parallel → one effect | L |
| PXG-045 | ⬜ | Input validation & sanitisation | Strict DTO validation (`additionalProperties:false`, body-size limit, phone/landmark/rating/comment rules), HTML-free text; tests E per endpoint incl. fuzz-style cases | M |
| PXG-046 | ⬜ | Rate limiting hardening | Tune policies from PXG-023/059/066; per-route config; `Retry-After`; metrics; documented single-instance limitation and Redis-backed adapter as the upgrade path; load test of limits | M |
| PXG-047 | ⬜ | Error handling & logging | JSON logs, correlation IDs, PII masking filter, no stack traces to clients; tests that grep logs for tokens/phones | M |
| PXG-077 | ⬜ | Observability, health & backups | `/health/live`, `/health/ready` (DB + worker heartbeat), metrics, alert rules (push failure rate, worker lag, error rate), backup schedule + **restore drill** on the chosen free tier | L |
| PXG-078 | ⬜ | Load & soak test | Script (e.g. k6) simulating 20 vendors, 200 orders/day with bursts; targets: nearby p95 < 500 ms, order creation p95 < 800 ms, zero double-accepts, worker lag < 60 s | M |

### Epic 4.2 — Admin Operations

**US-4.2.1 — Investigate any order.** *As an operator, I want to search orders and see their full history, so that I can resolve disputes by phone.*

Acceptance criteria
- AC1 — Given `GET /admin/orders` with filters `status`, `from`, `to`, `vendorId`, `phone`, `publicRef`, paginated, then I find the order.
- AC2 — Given `GET /admin/orders/{id}`, then I see the order, all offers (with timings and notify status) and the complete event timeline.

**US-4.2.2 — Fix exceptional cases.** *As an operator, I want controlled overrides with a mandatory reason, so that I can resolve stuck situations without touching the database.*

Acceptance criteria
- AC1 — Given `POST /admin/orders/{id}/override {action, reason, vendorId?, price?}` with action ∈ `CANCEL`, `FORCE_LIVREE`, `MARK_ECHOUEE`, `UNLOCK_CODE`, `REDISPATCH_TO_VENDOR`, then the transition is validated by the state machine, applies, and writes an `ADMIN_OVERRIDE` event + audit entry with the reason.
- AC2 — `REDISPATCH_TO_VENDOR` may exceed the price rule **only** with an explicit `price` and reason (the phone-call-with-client path of D3).
- AC3 — An override without reason → 400; illegal transition → 409.

**US-4.2.3 — Track vendor onboarding.** *As an operator, I want to see for each vendor whether it is truly ready (session used, push registered, catalogue filled, open toggle used), so that the pilot starts with working vendors.*

Acceptance criteria
- AC1 — Given `GET /admin/vendors/{id}/onboarding`, then I see booleans and timestamps for: session issued, session last used, active push subscription, ≥ 1 available brand, last availability update, last successful test push.
- AC2 — Given `POST /admin/vendors/{id}/test-push`, then a harmless test notification is sent and its result recorded (answers G2 in real conditions).

| ID | Status | Task | Details | Est. |
|---|---|---|---|---|
| PXG-048 | ⬜ | Admin order list & detail | Filters per AC; detail includes offers + events; indexes verified with `EXPLAIN`; tests E | L |
| PXG-049 | ⬜ | Controlled overrides | Actions per AC; goes through the same state machine; audit + event; tests E each action, U illegal transitions | L |
| PXG-050 | ⬜ | Vendor onboarding checklist & test push | Endpoints per AC; onboarding summary also added as a column set on the vendor list; tests E | M |

### Epic 4.3 — Security & Privacy (new)

**US-4.3.1 — Secure by review, not by hope.** *As the owner, I want a documented security pass before real clients use the API, so that obvious API vulnerabilities are closed.*

Acceptance criteria
- AC1 — The OWASP API Top 10 checklist is completed with a finding/fix/accepted-risk per item.
- AC2 — Automated IDOR tests cover every `/vendor/**`, `/admin/**` and tracking route; dependency and secret scans run in CI with no critical findings.
- AC3 — CORS allows only the configured PWA origins; security headers set; token entropy and hashing verified by tests.

**US-4.3.2 — Keep only what we need.** *As the owner, I want client personal data minimised and purged, so that we respect our own policy and local law.*

Acceptance criteria
- AC1 — Given an order terminal for more than `DATA_RETENTION_DAYS`, then `client_phone` is replaced by an HMAC (peppered) and `client_location` and landmark are cleared by a scheduled job, keeping statistics intact.
- AC2 — Given the order form, then the privacy notice/consent text is provided by the API contract (`GET /legal/notice`, static) for the PWA to display. *(Legal review of Cameroonian obligations is a non-code action tracked in `status.md`.)*
- AC3 — Given the block list, then raw phones remain only there, with a stated retention rule.

| ID | Status | Task | Details | Est. |
|---|---|---|---|---|
| PXG-079 | ⬜ | Data retention & privacy | Retention job (idempotent, `FakeClock` tests), HMAC pepper in secrets, `GET /legal/notice`, retention rule in the runbook; I tests that statistics queries survive anonymisation | M |
| PXG-080 | ⬜ | Security review | OWASP API checklist, IDOR test matrix, CORS/headers, JWT secret rotation procedure, token-entropy tests, dependency + secret scanning in CI, written findings | L |

### Epic 4.4 — Documentation & Handover

**US-4.4.1 — Someone else can run the pilot.** *As the next developer or operator, I want accurate docs and a runbook, so that I can operate and extend the system without the original author.*

Acceptance criteria
- AC1 — The OpenAPI contract matches the implementation (contract tests against staging pass) and has examples for every endpoint.
- AC2 — The runbook covers: creating a vendor and delivering its token, rotating/revoking a session, reading the escalation list and what to say on the phone, blocking a phone, handling a locked code, what to do when push fails or the worker is down, restoring a backup, applying a retention purge.
- AC3 — `status.md` lists deviations from this plan and the final state of every decision D1–D20.

| ID | Status | Task | Details | Est. |
|---|---|---|---|---|
| PXG-051 | ⬜ | API documentation | Final OpenAPI with examples; contract tests; changelog of breaking changes C1→C5 | M |
| PXG-052 | ⬜ | Pilot runbook | Content per AC2, tested by someone who did not write it | M |
| PXG-053 | ⬜ | Final status.md + architecture notes | Deviations, ADR index, known limitations (single-instance rate limiting, no client push, pickup without fallback), pilot Go/No-Go checklist | S |

**Sprint 4 exit criteria**
- All critical paths tested including failure and concurrency; load targets met; restore drill done.
- Operator can investigate and fix any order from the admin API; onboarding checklist usable for 15–20 vendors.
- Security review closed; retention job live; runbook validated; **contract C5 published**; production promotion approval-gated.

---

## Out of scope (explicit)

Independent drivers/wallet (v2) · real SMS/WhatsApp as primary channel (only behind interfaces; activated if fake-order rate > 10 %) · client Mobile Money · stock quantities · native apps · languages other than French · promotions/complex pricing · client push notifications (D6) · automatic fallback for pickup orders (D4b) · automatic higher-price fallback (D3).

## Pilot success metrics

| Metric | Minimum (Go) | Target |
|---|---|---|
| Orders / vendor / week | ≥ 3 | ≥ 6 |
| Median response time per offer | ≤ 12 min | ≤ 8 min |
| % orders confirmed by ≥ 1 vendor (≤ 3 attempts) | ≥ 70 % | ≥ 80 % |
| % orders reaching "Livrée" (code entered) | ≥ 60 % | ≥ 75 % |
| "Vérifiée" rate | tracked separately | — |
| Fake-order rate | ≤ 10 % | ≤ 5 % |
| Average rating | ≥ 3.5 | ≥ 4.0 |

Definitions: `ProxiGaz_Project_Analysis.md` §6 (implemented by PXG-043).

## Suggested order of execution

1. **Now:** Epic 1.0 (PXG-054 → 063), finish PXG-012C → Epic 1.2 → Epic 1.3 → **C1**.
2. **Gate G1** (and start the manual test now if not done).
3. Sprint 2a: PXG-068, 069, 067 (if not done) → 022, 023, 024, 025, 070 → 026, 027, 028, 029 → **C2**.
4. **Gate G2**, then Sprint 2b: 071, 030, 031, 032 → 033, 072, 034, 073, 074 → **C3**.
5. Sprint 3: 035–038, 075 → 039, 040, 019 → 041, 076, 042, 043 → **C4**.
6. Sprint 4: 044–047, 077, 078 → 048–050 → 079, 080 → 051–053 → **C5**.

**Critical path:** Sprint 2a → 2b (the vertical slice and fallback). Everything before prepares it; everything after completes and hardens it.

## Estimated remaining effort (one full-time developer)

| Sprint | Estimate |
|---|---|
| 1 (remaining) | ~1.5 weeks |
| 2a | ~1.5–2 weeks |
| 2b | ~1.5–2 weeks |
| 3 | ~2 weeks |
| 4 | ~2 weeks |
| **Total** | **≈ 8.5–10 weeks** (+ ~50 % if part-time). Frontend not included. |

## Task ID map (v1.4 → v2.0)

| v1.4 | v2.0 |
|---|---|
| PXG-012A | ⛔ removed (superseded) |
| PXG-018 | ⛔ removed |
| PXG-019 | moved to Sprint 3 (Epic 3.2) |
| PXG-038 | redefined as the stale-order sweeper |
| PXG-044 | reduced to idempotency keys; race tests are PXG-073 |
| — | new: PXG-054 … PXG-080 |

## Document history

| Version | Date | Changes |
|---|---|---|
| 1.0 – 1.4 | 2026-10-05 → 10-29 | See previous history (User/Vendor aggregates, onboarding session, release gates) |
| 2.0 | 2026-10-31 | Full re-analysis. Stories + acceptance criteria per epic; Epic 1.0 added; Sprint 2 split; decisions D1–D20; gates G1/G2; contract milestones; Epic 4.3; ID map |