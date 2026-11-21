# ADR 0004: Fallback eligibility, ranking and pickup orders

- **Status:** Accepted
- **Date:** 2026-11-18
- **Source:** Decision D4, D4b in [`ProxiGaz_Project_Analysis.md` §3](../ProxiGaz_Project_Analysis.md#3-decisions-register)

## Context

Fallback must be deterministic and testable. For a pickup order, a fallback would send the client to a place they did not choose.

## Decision

**Eligible:** same brand and size, vendor active, open now, brand currently available, not already offered for this order, within the search radius of the client (delivery).

**Ranking:** distance ascending, reliability descending (NULL counts as a neutral 50), total ascending, vendor id.

**Pickup orders (D4b)** have no automatic fallback: they end `EXPIREE` and appear in the admin list. Revisit with pilot data.

## Consequences

Ties are always broken by vendor id, so tests can assert the exact next vendor. Some pickup orders expire that a fallback could have saved.

## Implementation state

Pending — fallback, Sprint 3.
