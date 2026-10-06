# ProxiGaz — Project Status

Last updated: 2026-10-05 (post-review corrections)

## Critical Gate

**DO NOT start Sprint 0 coding until the manual 2–3 week test has produced numbers.**

The entire cadrage is built on the principle: prove demand and vendor response manually before writing production code.  
Current date relative to specs (02/10) means the manual test is either ongoing or not finished.  
Coding starts only after explicit go from the manual test results (or an explicit written decision to waive it).

## Sprint progress

| Sprint                                | Status      | Notes                                      |
|---------------------------------------|-------------|--------------------------------------------|
| Manual Test (2–3 weeks)               | In progress / Required first | Must produce the Go/No-Go numbers |
| 0 — Foundations & Core Infrastructure | Blocked     | Blocked until manual test results          |
| 1 — Vendor & Catalog Management       | Blocked     |                                            |
| 2 — Order Flow (Vertical Slice)       | Blocked     | Open decision on fallback price rule must be closed before this sprint |
| 3 — Fallback, Delivery & Reviews      | Blocked     |                                            |
| 4 — Hardening, Admin & Pilot Ready    | Blocked     |                                            |

## Use case status

| ID       | Name / Description                                      | Implemented | Unit tested | E2E tested | Notes |
|----------|---------------------------------------------------------|-------------|-------------|------------|-------|
| AUTH-01  | Admin login (JWT / session)                             | No          | No          | No         |       |
| AUTH-02  | Vendor session validation (long-lived token by admin)   | No          | No          | No         |       |
| VEN-01   | Admin creates / updates vendor + location (PostGIS)     | No          | No          | No         |       |
| VEN-02   | Admin activates / deactivates vendor                    | No          | No          | No         |       |
| VEN-03   | Admin creates long-lived VendorSession                  | No          | No          | No         |       |
| VEN-04   | CRUD VendorBrand (prices with/without exchange)         | No          | No          | No         |       |
| VEN-05   | Availability rule (`is_available AND available_until > now() AND is_open_now`) | No | No | No | Query-time computation |
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

- status.md previously suggested starting Sprint 0. Corrected: coding is blocked until manual test numbers exist.
- Anti-abuse was too weak (only max 2 orders per phone). Added rate limiting by IP/device requirement.
- Fallback timing and price rule were left ambiguous. Now flagged as open decisions.
- Clock port: always prefer controlled/fake clock for tests; never rely on real 10 min or 24 h waits.

## Architecture notes relevant to next task

- Must use PostgreSQL + PostGIS.
- Ports required from day one: `OtpProvider`, `NotificationChannel`, `GeoQueryPort`, `Clock` (controlled clock mandatory for tests).
- Order state machine and `order_offers` are central.
- Tracking must use `tracking_token`.
- Availability is computed at query time.
- Rate limiting (IP + device/fingerprint) + phone block list + vendor reporting are all required for anti-abuse.
- Personal data (phone + location) → check Cameroon data protection law before pilot launch.
- All work must follow `docs/ProxiGaz_Implementation_Guideline.md`.

## Next recommended action

1. Finish the manual 2–3 week test and collect the Go/No-Go numbers.
2. Close the open decisions (especially fallback price rule).
3. Only then start Sprint 0, beginning with PXG-001, following the Implementation Guideline strictly.
