# Architecture Decision Records

One short file per decision. ADRs 0001–0020 ratify decisions D1–D20 of the
[project analysis](../ProxiGaz_Project_Analysis.md#3-decisions-register) (ADR number = decision
number; D4b is part of ADR 0004). Later decisions continue the sequence.

An accepted ADR is changed only by an **amendment** inside it (dated) or by a new ADR that
supersedes it; never by silently editing the decision. The "Implementation state" section is kept
current as tasks land.

| ADR | Decision | Source | Status |
|-----|----------|--------|--------|
| [0001](0001-demand-gate-g1.md) | Demand gate G1 before Sprint 2a | D1 | Accepted |
| [0002](0002-offer-ttl-and-attempts.md) | Offer TTL and attempt limit | D2 | Accepted |
| [0003](0003-fallback-price-rule-b.md) | Fallback price rule B | D3 | Accepted |
| [0004](0004-fallback-eligibility-and-ranking.md) | Fallback eligibility, ranking and pickup orders | D4, D4b | Accepted |
| [0005](0005-money-model.md) | Money model on orders and offers | D5 | Accepted |
| [0006](0006-no-client-push.md) | No client push notifications in the MVP | D6 | Accepted |
| [0007](0007-confirmation-code-handling.md) | Confirmation code handling | D7 | Accepted |
| [0008](0008-locked-confirmation-code.md) | Locked confirmation code after 3 wrong attempts | D8 | Accepted |
| [0009](0009-vendor-withdrawal.md) | Vendor withdrawal after acceptance | D9 | Accepted |
| [0010](0010-tracking-token-and-public-ref.md) | Tracking token and public reference | D10 | Accepted |
| [0011](0011-vendor-session-token.md) | Vendor session token | D11 | Accepted |
| [0012](0012-open-now-on-vendor.md) | `is_open_now` belongs to the vendor | D12 | Accepted |
| [0013](0013-time-handling.md) | Time handling | D13 | Accepted |
| [0014](0014-phone-number-format.md) | Phone number format | D14 | Accepted |
| [0015](0015-error-model-rfc7807.md) | Error model: RFC 7807 problem details | D15 | Accepted |
| [0016](0016-privacy-of-contact-details.md) | Contact details revealed only after acceptance | D16 | Accepted |
| [0017](0017-rate-limiting.md) | Rate limiting | D17 | Accepted |
| [0018](0018-reliability-score.md) | Vendor reliability score | D18 | Accepted |
| [0019](0019-client-data-retention.md) | Client data retention | D19 | Accepted |
| [0020](0020-settings-from-environment.md) | Settings come from environment configuration | D20 | Accepted |
| [0021](0021-keep-demo-slice.md) | Keep the Demo slice | Owner decision, 2026-11-17 | Accepted |
| [0022](0022-unit-of-work-transaction-boundary.md) | UnitOfWork port as the use-case transaction boundary | PXG-061 | Accepted |
| [0023](0023-append-only-admin-audit-log.md) | Append-only admin audit trail | PXG-061 | Accepted |
| [0024](0024-contract-versioning-and-breaking-gate.md) | Contract versioning and breaking-change gate | PXG-060 | Accepted |
| [0025](0025-logging-policy.md) | Logging policy | PXG-061 | Accepted |

## Template

```markdown
# ADR NNNN: <title>

- **Status:** Proposed | Accepted | Rejected | Superseded by ADR NNNN
- **Date:** YYYY-MM-DD
- **Source:** <decision id, task or owner decision>

## Context

## Decision

## Consequences

## Implementation state
```
