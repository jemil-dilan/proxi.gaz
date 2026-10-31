# ProxiGaz — Project Re-Analysis (backend)

**Version:** 1.0 — 2026-10-31
**Inputs analysed:** Cadrage v1.4, MVP Specification v1.2, Sprint Plan v1.4, `main.yaml` (OpenAPI `2026-W40`).
**Not available to me:** Cadrage v1.5, Manual Test Plan, `ProxiGaz_Implementation_Guideline.md`, `status.md`, `docs/RELEASE_AND_DEPLOYMENT.md`, source code. Anything marked **(verify)** depends on them.
**Companion document:** `ProxiGaz_Sprint_Plan.md` v2.0 (built from this analysis).

---

## 1. What we are building

A web platform (PWA) for Cameroon that lets a household find an **open vendor with a gas bottle available nearby**, see the **total price before ordering**, and place an order that the vendor confirms. Mobile-money payment, drivers, stock quantities and native apps are out of scope.

### 1.1 Actors

| Actor | Identity | Channel | Main needs |
|---|---|---|---|
| **Client** | No account. Phone number (unverified in pilot) + random `tracking_token` | Public PWA, polling | Discover, quote, order, track, cancel, confirm reception, review |
| **Vendor** | Opaque long-lived session token created by admin (hash stored) | Vendor PWA + Web Push on low-end phones | Receive offers, accept/refuse, toggle availability and "open now", deliver with 4-digit code, report fake orders |
| **Admin / operator** | JWT (email + password), `User` with Admin role | Admin back-office | Onboard vendors, escalate silent offers (call after 5 min), block phones, override orders, read pilot KPIs |
| **System** | n/a | Workers | Expire offers, dispatch fallback, sweep stale orders, compute scores |

### 1.2 Core flow

```
Client: nearby → quote → create order ──► Order EN_ATTENTE + Offer #1 (TTL)
                                              │
        Vendor accepts ───────────────────────┼──► CONFIRMEE ─► EN_COURS ─► LIVREE (4-digit code) ─► LIVREE_VERIFIEE ─► review
        Vendor refuses / offer expires ───────┴──► fallback offer #2, #3 (rule B) ─► else EXPIREE
        Silent > 5 min ──► admin "needs attention" list ──► operator calls vendor
```

### 1.3 Non-negotiable constraints from the documents

- Demand must be proven first (manual test, 2–3 weeks): ≥ 20 orders/week from week 2, ≥ 60 % confirmed, ≥ 50 % of vendors delivering ≥ 1 order; stop rule below half of these after 3 weeks.
- No OTP on screen; protections = max 2 active orders/phone, rate limiting, 4-digit code (3 tries), fake-order report, block list. Fake-order rate > 10 % triggers reverse-WhatsApp OTP (behind `OtpProvider`).
- Notifications = Web Push behind `NotificationChannel`; manual escalation after 5 min.
- Offers: 10–12 min, max 3 attempts, table `order_offers`; availability computed at query time.
- No recurring messaging cost in pilot; free-tier hosting in a European region.
- Data minimisation; random tracking token; CGU make the vendor responsible for the product.

---

## 2. Reconciliation of the four sources

| # | Finding | Evidence | Resolution in Plan v2.0 |
|---|---|---|---|
| R1 | Plan cites **Cadrage v1.5**, but its history and the files provided are v1.4 | Plan header vs history | PXG-062: record the version baseline; supply v1.5 or correct the header |
| R2 | Plan schema names (`admin_users`, `vendors`) contradict the User-aggregate decision and the real `t_`/`c_` naming | PXG-005, PXG-009 vs PXG-012B | Table names updated; PXG-005/009 marked as superseded by the implemented model |
| R3 | `is_open_now` is listed under **VendorBrand** but a shop is open or closed as a whole | PXG-016 | Moved to **Vendor** (`is_open_now`) — Decision D12 |
| R4 | Spec Sprint 3 holds fallback; plan Sprint 2 holds fallback **and** the slice (13 tasks / 2 weeks) | Spec §7 vs plan | Sprint 2 split into **2a** (slice) and **2b** (push, expiry, fallback) |
| R5 | Reliability score (PXG-019) scheduled in Sprint 1 although it needs order data; plan also scores "cancellations" that nothing can generate | PXG-019 | Moved to Sprint 3; cancel/withdraw flows added |
| R6 | `PXG-024` snapshots price on the order; spec snapshots on each **offer** | PXG-024 vs Spec §3 | Both: `quoted_total` on order, `*_snapshot` on offer, `final_total` at acceptance — D5 |
| R7 | Contract: `VendorUpdateDTO.required` lists `isActive` which is not a property | `main.yaml` | PXG-057 |
| R8 | Contract: `address` (create) vs `addressText` (update/read) | `main.yaml` | PXG-057 |
| R9 | Contract: `Demo` endpoints + OAuth2 scheme are template leftovers; vendor opaque token is declared as JWT | `main.yaml` | PXG-054, PXG-056 |
| R10 | Contract: no error schema; most operations lack 401/403/409/429 | `main.yaml` | PXG-055 |
| R11 | Contract version `2026-W40` vs annotated SemVer tags in PXG-012C | `main.yaml` vs plan | PXG-060 |
| R12 | Plan says "notify client via NotificationChannel" but clients have no subscription | PXG-028 | D6: clients poll the tracking endpoint |
| R13 | No task delivers: public nearby endpoint, quote, vendor self-service availability, client cancel, vendor withdraw, vendor history | Plan scan | PXG-065/066/067/070/075 + 027 |
| R14 | Plan is backend-only, yet slice demos need a client | Exit criteria Sprint 2 | PXG-063: scripted API demos; contract milestones per sprint |
| R15 | Concurrency tests scattered (023, 034, then 044 in Sprint 4) | Plan | PXG-073 in Sprint 2b; 044 limited to idempotency keys |
| R16 | PXG-018 (auto-expire job) contradicts "query-time is truth" | Plan text | Removed; PXG-072 covers offer expiry only |
| R17 | Spec lists the manual **Web Push test on vendor phones** before coding; plan has no gate for it | Spec §1.4 | Gate G2 before PXG-030 |

---

## 3. Decisions register

Status **Proposed** = my recommendation, needs your ratification (record as ADR in PXG-062). Items marked **Spec** already come from the documents.

| ID | Decision | Rationale | Status |
|---|---|---|---|
| D1 | **Gate G1**: manual test numbers (or a dated written waiver in `status.md`) required before PXG-022 | Cadrage principle; Sprints 0–1 are low-regret, Sprint 2+ is the real investment | Proposed |
| D2 | `OFFER_TTL_SECONDS=600`, `OFFER_MAX_ATTEMPTS=3` (both configurable), escalation at 300 s. Worst case 30 min, not 36 | Low end of the spec range; consistent with median ≤ 12 min KPI | Proposed |
| D3 | **Fallback price rule B**: automatic fallback only to offers with `total ≤ quoted_total`. Higher prices only through admin override after a phone call with the client. Log how many candidates were excluded by price (`FALLBACK_PRICE_EXCLUDED`) | Option A needs a client notification channel that does not exist; spec allows B | Proposed |
| D4 | **Fallback eligibility**: same brand + size, vendor active, open now, brand currently available, not already offered for this order, within search radius of the client (delivery). **Ranking**: distance ↑, reliability ↓ (NULL = neutral 50), total ↑, vendor id | Deterministic and testable | Proposed |
| D4b | **Pickup orders have no automatic fallback** (client would have to go elsewhere); they end `EXPIREE` and appear in the admin list. Revisit with pilot data | Avoid sending a client to an unexpected place | Proposed |
| D5 | Money model: `quoted_total` (order), `unit_price_snapshot` + `delivery_fee_snapshot` + `total_snapshot` (offer), `final_total` (order, set at acceptance, always ≤ quoted) | Price integrity + KPI data | Proposed |
| D6 | **No client push in MVP.** Client polls `GET /orders/track/{token}`; `NotificationChannel` serves vendors and admin only | Clients are anonymous; keeps cost at zero | Proposed |
| D7 | Confirmation code is generated at order creation, returned to the client (create response + tracking while `CONFIRMEE`/`EN_COURS`), **never** sent to vendor endpoints. Stored in clear (the client must be able to display it); protection = 3 attempts + lock | Required by the display need | Proposed |
| D8 | After 3 wrong codes: code is **locked**, order stays `EN_COURS`, appears in "needs attention"; admin resolves (force `LIVREE` or `ECHOUEE`) with audit | A vendor may have delivered and mistyped | Proposed |
| D9 | Vendor withdrawal after accepting → `ECHOUEE` (terminal, counts against reliability); admin may re-dispatch manually | Simple; avoids hidden retry loops | Proposed |
| D10 | `tracking_token` ≥ 128 bits random, **stored hashed** (SHA-256); raw value returned once. Order also has a short `public_ref` (e.g. `PG-7K3M9`) for phone/WhatsApp support | A DB leak must not expose tracking links | Proposed |
| D11 | Vendor session token: ≥ 256 bits, prefix for lookup + SHA-256 hash, `last_used_at`, no expiry but revocable/rotatable; valid token on **inactive** vendor → `403 PROXIGAZ-403-002` | Long sessions are a spec requirement | Proposed |
| D12 | `is_open_now` is a **Vendor** attribute; `is_available` + `available_until` stay on **VendorBrand**. Vendor availability rule: `vendor.is_active ∧ vendor.is_open_now ∧ brand.is_available ∧ brand.available_until > :now` | Domain correctness; spec's single "Ouvert maintenant" toggle | Proposed |
| D13 | Time: all `TIMESTAMPTZ` in UTC; API in ISO-8601 UTC; display in Africa/Douala is a frontend concern; SQL receives `:now` from the `Clock` port, never `now()` | Testability with `FakeClock` | Proposed |
| D14 | Phone: store E.164 `+237` + 9 digits (`^\+237[26]\d{8}$`), accept local input formats, normalise at the edge | Uniform limits and block list | Proposed |
| D15 | Errors: RFC 7807 `application/problem+json` with `code` = `PROXIGAZ-{HTTP}-{NNN}`, `correlationId`, optional `errors[]` | Matches the `PROXIGAZ-409-000` convention in the contract | Proposed |
| D16 | Public nearby does **not** expose vendor phone; vendor phone and exact client coordinates/phone are revealed only **after acceptance** (to client / vendor respectively) | Privacy + prevents bypassing the platform before confirmation | Proposed |
| D17 | Rate limiting behind a `RateLimiter` port, in-memory token bucket (single instance in pilot); `X-Forwarded-For` trusted only from the configured proxy | Free-tier single instance; no Redis | Proposed |
| D18 | Reliability score: rolling 30 days, `100 × (1 − (0.5·refused + 1.0·expired + 2.0·withdrawn) / offers)` clamped to [0,100]; `NULL` below 5 offers; weights configurable; admin-only visibility | Simple, explainable | Proposed |
| D19 | Client data retention: 180 days after terminal state, client phone replaced by HMAC and location nulled; block list keeps raw phone | Data minimisation (cadrage §8) | Proposed — legal check (non-code) |
| D20 | Settings (radius, TTL, attempts, escalation delay) come from **environment config**; read-only `GET /admin/settings` | Avoid building a settings CRUD for the pilot | Proposed |

---

## 4. Domain model

### 4.1 Order state machine

| From | Event / actor | To | Notes |
|---|---|---|---|
| — | Client creates order | `EN_ATTENTE` | Offer #1 created, `expires_at = now + TTL` |
| `EN_ATTENTE` | Vendor accepts current offer | `CONFIRMEE` | `confirmed_vendor_id`, `final_total` set |
| `EN_ATTENTE` | Offer refused/expired and fallback possible | `EN_ATTENTE` | New offer, `attempt_count + 1` |
| `EN_ATTENTE` | Attempts exhausted, no eligible vendor, or pickup order | `EXPIREE` | Terminal; reason recorded |
| `EN_ATTENTE`, `CONFIRMEE` | Client cancels / admin cancels | `ANNULEE` | Terminal; open offer → `CANCELLED` |
| `CONFIRMEE` | Vendor starts delivery/pickup | `EN_COURS` | |
| `CONFIRMEE`, `EN_COURS` | Vendor withdraws / admin marks failed / delivery timeout | `ECHOUEE` | Terminal |
| `EN_COURS` | Vendor submits correct code | `LIVREE` | `delivered_at`; active slot freed |
| `EN_COURS` | 3 wrong codes | `EN_COURS` + `code_locked_at` | Needs admin (D8) |
| `LIVREE` | Client confirms reception | `LIVREE_VERIFIEE` | Unlocks review |
| `LIVREE` | 24 h without confirmation | `LIVREE` + `auto_closed_at` | Counts as Livrée, review stays locked |

"Active" statuses (count toward the 2-order limit): `EN_ATTENTE`, `CONFIRMEE`, `EN_COURS`.

### 4.2 Offer state machine

`PROPOSED → ACCEPTED | REFUSED | EXPIRED | CANCELLED`. Exactly one `PROPOSED` offer per order at any time (partial unique index).

### 4.3 Data model (target; each sprint adds only the tables it needs)

| Table | Key columns | Constraints / indexes |
|---|---|---|
| `t_users` (exists) | id, email, password_hash, role | unique email |
| `t_vendors` (exists) | id, user_id, name, phone, location `geography(Point,4326)`, address_text, is_station, delivery_fee_type, delivery_fee_amount, is_active, **is_open_now**, reliability_score, reliability_updated_at | unique user_id, unique phone, **GIST(location)** |
| `t_vendor_sessions` (exists) | id, vendor_id, token_prefix, token_hash, created_at, last_used_at, revoked_at | unique token_hash; one active per vendor |
| `t_brands` (new) | id, name, is_active | unique lower(name) |
| `t_vendor_brands` (new) | id, vendor_id, brand_id, size_kg, price_with_exchange, price_without_exchange (FCFA int), is_available, available_until | unique (vendor_id, brand_id, size_kg); prices > 0 |
| `t_orders` (new) | id, public_ref, tracking_token_hash, client_phone, client_location, client_landmark, has_empty_bottle, is_pickup, status, requested_vendor_id, confirmed_vendor_id, brand_id, size_kg, unit_price, delivery_fee, quoted_total, final_total, confirmation_code, code_attempts, code_locked_at, attempt_count, active_slot, created_at, confirmed_at, started_at, delivered_at, client_confirmed_at, auto_closed_at, closed_at, terminal_reason, version | unique public_ref, unique token_hash, **partial unique (client_phone, active_slot) WHERE active_slot IS NOT NULL**, index (status, created_at) |
| `t_order_offers` (new) | id, order_id, vendor_id, vendor_brand_id, attempt_no, status, unit_price_snapshot, delivery_fee_snapshot, total_snapshot, created_at, expires_at, responded_at, refusal_reason, notified_at, notify_status | unique (order_id, attempt_no), unique (order_id, vendor_id), **partial unique (order_id) WHERE status='PROPOSED'**, index (status, expires_at) |
| `t_order_events` (new, append-only) | id, order_id, offer_id, type, from_status, to_status, actor_type, actor_id, metadata jsonb, created_at | index (order_id, created_at) |
| `t_order_reports` (S3) | id, order_id, vendor_id, reason, note, status (OPEN/CONFIRMED/DISMISSED), created_at, reviewed_by | unique (order_id, vendor_id) |
| `t_reviews` (S3) | id, order_id, vendor_id, rating 1–5, comment, created_at | unique (order_id) |
| `t_blocked_phones` (S3) | phone, reason, blocked_by, created_at, unblocked_at | unique active phone |
| `t_push_subscriptions` (S2b) | id, vendor_id, endpoint, p256dh, auth, created_at, disabled_at | unique endpoint |
| `t_admin_audit_log` (S1) | id, admin_user_id, action, target_type, target_id, metadata jsonb, created_at | index (target_type, target_id) |
| `t_idempotency_keys` (S4) | key, scope, request_hash, response_status, response_body, expires_at | unique (scope, key) |

---

## 5. API inventory by sprint

| Sprint | Audience | Endpoints |
|---|---|---|
| **S1** (partly done) | Admin | `POST/GET /admin/vendors`, `GET/PUT /admin/vendors/{id}`, `PUT …/status`, `POST/DELETE …/session`, `POST/GET/PUT/DELETE /admin/vendors/{id}/brands[/{brandId}]`, `POST /admin/auth/login`, `GET /admin/auth/me`, `GET /admin/settings`, `GET /admin/audit-log`, `GET/POST /admin/brands` |
| | Vendor | `GET /vendor/session`, `GET /vendor/brands`, `PUT /vendor/brands/{id}/availability`, `PUT /vendor/open-status` |
| | Public | `GET /vendors/nearby`, `POST /orders/quote` |
| **S2a** | Public | `POST /orders`, `GET /orders/track/{token}`, `POST /orders/track/{token}/cancel` |
| | Vendor | `GET /vendor/offers`, `GET /vendor/orders`, `GET /vendor/orders/{id}`, `POST /vendor/offers/{id}/accept`, `POST /vendor/offers/{id}/refuse` |
| **S2b** | Vendor | `POST/DELETE /vendor/push-subscriptions`, `GET /vendor/push-config` |
| | Admin | `GET /admin/orders/needs-attention`, `POST /admin/offers/{id}/escalation-ack` |
| **S3** | Vendor | `POST /vendor/orders/{id}/start`, `…/complete`, `…/withdraw`, `…/report` |
| | Public | `POST /orders/track/{token}/confirm-reception`, `POST …/review` |
| | Admin | `GET/POST/DELETE /admin/blocked-phones`, `GET/PUT /admin/reports`, `GET /admin/metrics/pilot` |
| **S4** | Admin | `GET /admin/orders`, `GET /admin/orders/{id}`, `POST /admin/orders/{id}/override`, `GET /admin/vendors/{id}/onboarding`, `POST /admin/vendors/{id}/test-push` |
| | Ops / public | `GET /health/live`, `GET /health/ready`, `GET /legal/notice` |

---

## 6. Pilot KPI definitions (so that the numbers are computable and comparable)

| KPI | Numerator / denominator (period filter on `orders.created_at`) |
|---|---|
| Orders / vendor / week | Orders with `confirmed_vendor_id = v` and status ≥ `CONFIRMEE`, per ISO week |
| Median response time per offer | Median of `responded_at − created_at` over offers `ACCEPTED` or `REFUSED`; **expired offers excluded** and reported separately as `expiryRate` |
| % confirmed by ≥ 1 vendor (3 attempts) | Orders that reached `CONFIRMEE` or later ÷ orders created, excluding orders cancelled by the client before any offer outcome |
| Taux « Livrée » | Orders `LIVREE`/`LIVREE_VERIFIEE` ÷ orders that reached `CONFIRMEE`, restricted to orders now in a closed state or older than 24 h |
| Taux « Vérifiée » (separate) | `LIVREE_VERIFIEE` ÷ (`LIVREE` + `LIVREE_VERIFIEE`) |
| Average rating | Mean of `t_reviews.rating`, with count |
| Fake-order rate | Distinct orders with ≥ 1 report not `DISMISSED` ÷ orders created. **OTP trigger**: rate > 10 % over a rolling 7 days with ≥ 30 orders |

---

## 7. Risk register

| # | Risk | Impact | Likelihood | Mitigation (task) |
|---|---|---|---|---|
| K1 | Demand not proven; code built for nothing | Very high | Unknown (**verify**) | G1 gate; Sprint 2 only after Go or waiver |
| K2 | Web Push unreliable on vendors' low-end phones | High | Medium | G2 test; escalation list (PXG-032); admin test push (PXG-050) |
| K3 | Expiry worker not running (free tier sleeping) | High | High | Query-time guards, heartbeat, always-on instance (PXG-072, 074) |
| K4 | Race: accept vs expiry, double-submit | High | Medium | Conditional updates, locking, PXG-073, PXG-044 |
| K5 | Fallback price rule B leaves many orders unmatched | Medium | Medium | `FALLBACK_PRICE_EXCLUDED` data, revisit after pilot |
| K6 | Fake orders / abuse without OTP | Medium | Medium | Rate limits, block list, reports, 10 % trigger (PXG-023/041/042/043) |
| K7 | Solo-developer capacity: ~8–10 more full-time weeks, no frontend in plan | High | High | Sprint split, scripted demos, explicit frontend track |
| K8 | Contract churn breaks the frontend | Medium | Medium | PXG-054–060 before more endpoints; contract milestones |
| K9 | Free-tier DB limits (backups, connections, PostGIS availability) | Medium | Medium | PXG-077 backup/restore drill |
| K10 | Personal-data obligations in Cameroon not verified | Medium | Medium | PXG-079 + legal check |
| K11 | Latitude/longitude order mistakes in geo code | Medium | Medium | Cameroon bounding-box validation, known-distance tests |

---

## 8. Traceability: requirement → tasks

| Source requirement | Tasks |
|---|---|
| Manual test before code (Cadrage §1–2) | G1, PXG-062 |
| Acquisition tested in manual phase (§3) | Out of backend scope; `public_ref` supports operator follow-up (PXG-022) |
| Compare vendors, check availability, total price, consigne (§4) | PXG-017, 020, 065, 066, 067 |
| No on-screen OTP; `OtpProvider` ready (§5.1) | PXG-007/008 (done), PXG-043 trigger flag |
| Max 2 active orders, rate limit, 4-digit code, fake report, vendor call (§5.1) | PXG-023, 046, 036, 041, 042 |
| Web Push + `NotificationChannel`; escalation after 5 min (§5.2) | PXG-030, 031, 032, 071 |
| 10–12 min offers, max 3, `order_offers`, price rule (§5.3) | PXG-033, 072, 034 |
| Availability + `available_until` at query time + "open now" (§5.4) | PXG-017, 064, 065 |
| Consigne with double price, delivery fee, pickup (§5.4) | PXG-016, 067, 022 |
| KPIs corrected (§6) | PXG-043 (+ §6 above) |
| Random tracking token (§8) | PXG-025, D10 |
| Data minimisation, local law (§8) | PXG-079 |
| `order_events` for audit and KPIs (Spec §3) | PXG-022 onward; every transition writes an event |
| Long vendor sessions created by admin (Spec §3) | PXG-012B, 015 (done) |
| Reviews only after client confirmation (Spec §8) | PXG-037, 039 |
| Admin can escalate manually (Spec §8) | PXG-032, 049 |
| No recurring messaging cost (Spec §8) | D6, `NotificationChannel` adapters |

---

## 9. Questions that need your answer

1. Has the manual test run, and with what numbers? If not, will you record a written waiver (G1)?
2. Do you ratify decisions D2 (10 min × 3), D3 (price rule B) and D4b (no pickup fallback)? These are the three that change behaviour the most.
3. Who builds the PWA (client + vendor + admin), and when? The plan now publishes contract milestones so that work can start early.
4. Which stack is used? `localhost:8080`, `DTO` suffixes and the `Demo` template suggest Spring Boot **(verify)**; the plan stays stack-neutral.
