# ADR 0002: Offer TTL and attempt limit

- **Status:** Accepted
- **Date:** 2026-11-18
- **Source:** Decision D2 in [`ProxiGaz_Project_Analysis.md` §3](../ProxiGaz_Project_Analysis.md#3-decisions-register)

## Context

The spec gives a range for how long a vendor has to answer an offer and how many vendors are tried. The KPI target is a median of 12 minutes or less to acceptance.

## Decision

`OFFER_TTL_SECONDS=600` and `OFFER_MAX_ATTEMPTS=3`, both configurable (ADR 0020), with escalation to the admin at 300 s. Worst case: 30 minutes, not 36.

## Consequences

Low end of the spec range; a client waits at most 30 minutes before the order expires.

## Implementation state

Pending — offers and expiry, Sprint 2 (PXG-022 onward).
