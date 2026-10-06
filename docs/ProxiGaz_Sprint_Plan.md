# ProxiGaz — Backend Sprint Plan (MVP)

**Version:** 1.1  
**Based on:** Cadrage v1.5 + MVP Specification v1.2 + Manual Test Plan  
**Language:** English  
**Scope:** Backend only (API, domain, persistence, jobs, admin endpoints). Frontend / PWA is out of scope of this plan.  
**Prerequisite:** Manual 2–3 week test must produce Go/No-Go numbers before any Sprint 0 coding. All sprints are **Blocked** until that gate is passed (see `../status.md`).

All work must follow `ProxiGaz_Implementation_Guideline.md`.

---

## Architecture Overview (must be respected)

Recommended modular structure (hexagonal / clean):

```
src/
  domain/           # pure business logic, entities, value objects, domain services
  application/      # use cases / application services, ports (interfaces)
  infrastructure/   # adapters (Postgres/PostGIS, Web Push, future OTP, etc.)
  interfaces/       # HTTP controllers, DTOs, validation
  jobs/             # background workers (offer expiry, availability cleanup, etc.)
```

**Key ports (interfaces):**
- `OtpProvider` (pilot = null or no-op; later = reverse WhatsApp / SMS)
- `NotificationChannel` (pilot = Web Push; escalation = manual admin call)
- `GeoQueryPort` (PostGIS)
- `Clock` (for testable time)

**Core aggregates / entities:**
- Vendor, VendorBrand (availability + prices)
- Order, OrderOffer, OrderEvent, OrderReport
- Review
- AdminUser, VendorSession
- PushSubscription

**Database:** PostgreSQL + PostGIS extension mandatory.

---

## Global Non-Functional Requirements

- All order status changes must be recorded in `order_events` (audit trail).
- Tracking links use `tracking_token` (UUID or secure random), never sequential IDs.
- Max 2 active orders per phone number (enforced at application + DB level if possible).
- Confirmation code: 4 digits, max 3 attempts.
- Offer timeout: configurable (currently proposed 10–12 min — to be decided; 3 × 12 min can reach 36 min and is likely too long).
- Max 3 fallback offers per order.
- Fallback price rule: open decision (client accepts new total vs only ≤ original price) — must be closed before implementing fallback.
- Availability query: `is_available = true AND available_until > now() AND is_open_now = true`.
- Reliability score computed from refusal / non-response / cancellation rates.

---

## Sprint 0 — Foundations & Core Infrastructure
**Goal:** Runnable project skeleton, database schema, core domain types, ports, and admin authentication.  
**Duration estimate:** 1–2 weeks (full-time)

### Epic 0.1 — Project Bootstrap
| ID | Task | Details | Acceptance |
|----|------|---------|------------|
| PXG-001 | Initialize repository & tooling | Create monorepo or backend repo, package manager, linting, formatting, CI basic pipeline (lint + test) | CI runs on PR |
| PXG-002 | Choose & configure runtime | Node (NestJS/Express) or equivalent. Document choice. Environment variable management. | App starts with `npm run start` / equivalent |
| PXG-003 | Database setup | PostgreSQL + PostGIS. Migration tool (e.g. Prisma, TypeORM, Knex, Flyway…). Connection from app. | Can run migrations against empty DB |
| PXG-004 | Config & secrets | Central config module. No secrets in code. Support for local / staging / production. | Different env files work |

### Epic 0.2 — Core Schema & Domain Foundations
| ID | Task | Details | Acceptance |
|----|------|---------|------------|
| PXG-005 | Create initial migrations | Tables: `admin_users`, `vendors`, `vendor_brands`, `orders`, `order_offers`, `order_events`, `order_reports`, `reviews`, `push_subscriptions`, `vendor_sessions`. Indexes including GIST on `vendors.location`. | Migrations apply cleanly; PostGIS index present |
| PXG-006 | Domain entities & value objects | OrderStatus enum + state machine helpers, Money (FCFA integer), GeoPoint, ConfirmationCode, TrackingToken, Availability rules | Unit tests for state transitions and availability calculation |
| PXG-007 | Ports definition | `OtpProvider`, `NotificationChannel`, `GeoQueryPort`, `Clock` interfaces | Interfaces exist and are injected |
| PXG-008 | Null / in-memory adapters | NullOtpProvider, LoggingNotificationChannel (or no-op), InMemory or real PostGIS GeoQuery, SystemClock + FakeClock | Can run tests without external services |

### Epic 0.3 — Admin Authentication
| ID | Task | Details | Acceptance |
|----|------|---------|------------|
| PXG-009 | Admin user model & repository | CRUD for admin users (email + hashed password) | Can create admin via seed or CLI |
| PXG-010 | JWT (or session) auth for Admin | Login endpoint, protected route middleware, password hashing (bcrypt/argon2) | Admin can log in and access protected route; invalid credentials rejected |
| PXG-011 | Seed data | At least 1 admin + 5–10 sample vendors with locations inside a pilot neighborhood (PostGIS points) | Seed script runs successfully |

**Sprint 0 Exit criteria:**  
- Migrations green  
- Admin can authenticate  
- Domain state machine unit-tested  
- Ports defined and null adapters in place  
- `../status.md` created

---

## Sprint 1 — Vendor & Catalog Management
**Goal:** Admin can fully manage vendors and their brands/prices/availability. Vendors can be activated.  
**Duration estimate:** 1–2 weeks

### Epic 1.1 — Vendor Management (Admin)
| ID | Task | Details | Acceptance |
|----|------|---------|------------|
| PXG-012 | Create / update Vendor | Name, phone, location (lat/lng → PostGIS), address_text, is_station, delivery_fee_type, delivery_fee_amount, is_active | Admin endpoints work; location stored as geography |
| PXG-013 | Activate / deactivate Vendor | `is_active` flag controlled by Admin only | Inactive vendors never appear in nearby search |
| PXG-014 | List / filter Vendors | Pagination, filter by is_active, text search on name | Admin list endpoint returns correct data |
| PXG-015 | VendorSession creation | Admin creates long-lived session token for a vendor (pilot onboarding). Token stored hashed or secure. | Vendor can later be authenticated with that token |

### Epic 1.2 — Vendor Brands & Availability
| ID | Task | Details | Acceptance |
|----|------|---------|------------|
| PXG-016 | CRUD VendorBrand | brand_name, size_kg, price_with_exchange, price_without_exchange, is_available, available_until, is_open_now | Prices and availability can be set |
| PXG-017 | Availability computation | Query helper: `isCurrentlyAvailable(brand) = is_available && available_until > now() && is_open_now` | Unit tests cover all combinations of flags and time |
| PXG-018 | Auto-expire availability (optional job) | Background job or query-time only. Prefer query-time as source of truth. | Expired brands are treated as unavailable |
| PXG-019 | Vendor reliability score (basic) | Calculate from historical refusals / expirations / cancellations. Store on vendor. | Score updates after relevant events (can be simple for now) |

### Epic 1.3 — Geo Queries
| ID | Task | Details | Acceptance |
|----|------|---------|------------|
| PXG-020 | Nearby vendors query | `ST_DWithin` + `ST_Distance`, filter is_active + currently available brands, order by distance | Returns correct vendors inside radius, sorted by distance |
| PXG-021 | Configurable search radius | Default e.g. 5 km, configurable per environment or admin setting | Radius change affects results |

**Sprint 1 Exit criteria:**  
- Admin can manage full vendor lifecycle  
- Nearby query works with PostGIS  
- Availability rules unit-tested  
- Sample data usable for next sprint

---

## Sprint 2 — Order Flow (Vertical Slice)
**Goal:** A client can create an order, a vendor can confirm or refuse it, basic notification is sent. End-to-end happy path works.  
**Duration estimate:** 2 weeks (critical path)

### Epic 2.1 — Order Creation
| ID | Task | Details | Acceptance |
|----|------|---------|------------|
| PXG-022 | Order creation use case | Input: client_phone, vendor_id, vendor_brand_id, has_empty_bottle, is_pickup, client_location, client_landmark, optional delivery_fee snapshot. Generate tracking_token + confirmation_code. Status = En attente. Create first OrderOffer. | Order + first offer persisted; events recorded |
| PXG-023 | Max 2 active orders + basic rate limiting | Before create: count active orders for phone (reject if ≥ 2). Also apply simple rate limiting by IP and by device/fingerprint. Enforce with a DB constraint or unique partial index where possible. **Must include a concurrent-request test.** | Unit + concurrent e2e tests pass |
| PXG-024 | Price snapshot | Store price_bottle, delivery_fee, total_price at creation time (never recalculate later from current brand prices) | Historical prices preserved |
| PXG-025 | Tracking by token | Public endpoint `GET /orders/track/:token` returns safe status info | Cannot enumerate orders by sequential ID |

### Epic 2.2 — Vendor Order Actions
| ID | Task | Details | Acceptance |
|----|------|---------|------------|
| PXG-026 | Vendor authentication (session) | Validate VendorSession token on vendor endpoints | Only the correct vendor can act on their offers |
| PXG-027 | List pending offers for vendor | Vendor sees offers in status `proposed` that belong to them and are not expired | Correct filtering |
| PXG-028 | Accept offer | Transition offer → accepted, order → Confirmée, record event, notify client (via NotificationChannel) | State machine respected; event written |
| PXG-029 | Refuse offer | Transition offer → refused, trigger fallback logic (or mark order if last attempt) | Correct status and next offer creation |

### Epic 2.3 — Notifications (pilot)
| ID | Task | Details | Acceptance |
|----|------|---------|------------|
| PXG-030 | Web Push adapter | Implement NotificationChannel with Web Push (VAPID). Store subscriptions. | Can send a push to a registered subscription |
| PXG-031 | Notify vendor on new offer | When OrderOffer is created, send push (if subscription exists) | Notification attempted; failure does not break order flow |
| PXG-032 | Manual escalation support | Admin endpoint or flag to mark “needs call”. Simple list of orders waiting > 5 min without response. | Admin can see orders that need manual call |

### Epic 2.4 — Offer Expiry & Fallback
| ID | Task | Details | Acceptance |
|----|------|---------|------------|
| PXG-033 | Fallback price rule (decision first) | **Open decision – must be closed before implementing the expiry worker.** Option A: client must explicitly accept the new total. Option B: only fall back to vendors with total ≤ original. Also re-evaluate offer duration (current 10–12 min × 3 can reach 36 min – too long). Document the chosen rule. | Rule is explicit and documented |
| PXG-034 | Expiry worker + fallback implementation | Periodically (or on read) mark offers past `expires_at` as expired. If attempts < 3, create next offer according to the rule decided in PXG-033. | Expiry + fallback works; max 3 offers respected; concurrent safety tested |

**Sprint 2 Exit criteria (critical):**  
A full vertical slice works:  
Client creates order → Vendor receives notification / sees offer → Vendor accepts → Order moves to Confirmée → Client can track via token.  
Fallback and expiry are implemented and tested.

---

## Sprint 3 — Delivery, Confirmation Code, Reviews & Events
**Goal:** Complete the order lifecycle to Livrée / Livrée & Vérifiée, reviews, and robust event history.  
**Duration estimate:** 1.5–2 weeks

### Epic 3.1 — Delivery Confirmation
| ID | Task | Details | Acceptance |
|----|------|---------|------------|
| PXG-035 | Start delivery / pickup | Vendor action → status En cours | Event recorded |
| PXG-036 | Submit confirmation code | Vendor submits 4-digit code. Max 3 attempts. Success → Livrée + delivered_at. Failure after 3 → blocked or Échouée. | Unit tests for attempt counting; e2e success & failure |
| PXG-037 | Client confirmation | Client confirms reception → Livrée & Vérifiée + client_confirmed_at. Unlocks review. | Only possible after Livrée |
| PXG-038 | 24 h auto-stats validation | Job or scheduled check: if Livrée and no client confirmation after 24 h, mark for stats but keep review locked. | Behavior documented and tested with FakeClock |

### Epic 3.2 — Reviews
| ID | Task | Details | Acceptance |
|----|------|---------|------------|
| PXG-039 | Create review | Only if order is Livrée & Vérifiée. rating 1–5 + optional comment. One review per order. | Constraint enforced |
| PXG-040 | Display aggregate rating | On vendor profile / nearby list, show average rating and count. | Correct calculation |

### Epic 3.3 — Reporting & Anti-abuse
| ID | Task | Details | Acceptance |
|----|------|---------|------------|
| PXG-041 | Report fake order | Vendor can create OrderReport with reason in one click. Reports feed the fake-order rate metric. | Report stored, visible to Admin, counted in KPI |
| PXG-042 | Phone block list | Admin can block a phone. Blocked phones cannot create new orders. | Enforced at order creation |
| PXG-043 | Basic metrics endpoints | Orders per day, confirmation rate, median response time, fake-order rate, etc. (for pilot KPIs). Fake-order rate > 10 % is the trigger to activate stronger OTP. | Admin can query key pilot indicators |

**Sprint 3 Exit criteria:**  
Full happy path to verified delivery + review.  
Failure paths (wrong code, expiry, fake report) covered by tests.  
Pilot KPI queries available.

---

## Sprint 4 — Hardening, Admin UX & Pilot Readiness
**Goal:** Production-ready enough for the limited pilot neighborhood. Observability, edge cases, documentation.  
**Duration estimate:** 1–2 weeks

### Epic 4.1 — Robustness
| ID | Task | Details | Acceptance |
|----|------|---------|------------|
| PXG-044 | Idempotency & concurrency | Protect order creation and offer acceptance against double-submit / race. | Concurrent tests pass |
| PXG-045 | Input validation & sanitization | Strict validation on all public endpoints (phone format, landmark required, etc.). | Invalid payloads rejected with clear errors |
| PXG-046 | Rate limiting (hardening) | Strengthen and tune the basic rate limiting introduced in PXG-023 (IP + device + phone). Add monitoring and clearer error responses. | Abuse attempts limited; tested under load |
| PXG-047 | Error handling & logging | Structured logging. No stack traces to client. Correlation IDs. | Logs usable for debugging pilot issues |

### Epic 4.2 — Admin Completeness
| ID | Task | Details | Acceptance |
|----|------|---------|------------|
| PXG-048 | Admin order list & filters | Filter by status, date, vendor, phone. See full event history. | Admin can investigate any order |
| PXG-049 | Manual offer / status override | Controlled admin actions for exceptional cases (with audit). | Overrides are logged in order_events |
| PXG-050 | Vendor onboarding checklist support | Endpoints or flags that help admin track which vendors have sessions, push subscriptions, etc. | Pilot onboarding is trackable |

### Epic 4.3 — Documentation & Handover
| ID | Task | Details | Acceptance |
|----|------|---------|------------|
| PXG-051 | API documentation | OpenAPI / Swagger up to date for all public and admin endpoints. | Docs match implementation |
| PXG-052 | Runbook for pilot | How to create vendor sessions, how to monitor escalation list, how to block a phone, what to do when push fails. | Written runbook exists |
| PXG-053 | Final status.md + architecture notes | Update status.md. Document any deviations from original plan. | Ready for pilot or next developer |

**Sprint 4 Exit criteria:**  
- All critical paths tested (including failure and concurrency)  
- Admin can operate the pilot  
- Runbook written  
- System ready for the chosen neighborhood with 15–20 vendors

---

## Out of Scope for this MVP Backend Plan (explicit)

- Independent drivers / livreur + prepaid wallet (v2)
- Real SMS or WhatsApp Business API as primary channel (only behind interfaces, activated later if fake-order rate > 10 %)
- Client-side Mobile Money payment
- Precise stock quantities (only available / unavailable)
- Native mobile applications
- Multi-language beyond French (can be added later)
- Complex pricing rules or promotions

---

## Pilot Success Metrics (backend must support measuring them)

From the final specification:

| Metric | Minimum (Go) | Target |
|--------|--------------|--------|
| Orders / vendor / week | ≥ 3 | ≥ 6 |
| Median response time per offer | ≤ 12 min | ≤ 8 min |
| % orders confirmed by ≥ 1 vendor (within 3 attempts) | ≥ 70 % | ≥ 80 % |
| % orders reaching “Livrée” (code entered) | ≥ 60 % | ≥ 75 % |
| Fake-order report rate | ≤ 10 % | ≤ 5 % |
| Average rating | ≥ 3.5 | ≥ 4.0 |

Backend must expose the data needed to calculate these (via `order_events`, `order_offers`, `order_reports`, `reviews`).

---

## Suggested Order of First Tasks (after manual test success)

1. PXG-001 → PXG-011 (Sprint 0 complete)
2. PXG-012 → PXG-021 (Sprint 1)
3. PXG-022 → PXG-034 (Sprint 2 – vertical slice – highest risk)
4. Then Sprint 3 and 4

**Critical path:** Sprint 2 vertical slice. Everything before it is preparation; everything after it is completion and hardening.

---

## Document History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-10-05 | Initial detailed backend sprint plan derived from Cadrage v1.4 + MVP Spec v1.2 |
| 1.1 | 2026-10-05 | Aligned with Cadrage v1.5. Sprints marked Blocked until manual test. Fallback price decision moved before expiry worker (PXG-033). Basic rate limiting + concurrent test added to PXG-023 (Sprint 2). Offer duration marked configurable/open. |
