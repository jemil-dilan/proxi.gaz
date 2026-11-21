# ADR 0019: Client data retention

- **Status:** Accepted
- **Date:** 2026-11-18
- **Source:** Decision D19 in [`ProxiGaz_Project_Analysis.md` §3](../ProxiGaz_Project_Analysis.md#3-decisions-register)
- **Condition:** a legal review must confirm the 180-day retention period before the pilot.

## Context

Data minimisation (Cadrage §8).

## Decision

180 days after an order reaches a terminal state, the client phone is replaced by an HMAC and the location is nulled. The block list keeps the raw phone.

## Consequences

KPIs still work on pseudonymised data. A legal review must confirm the retention period before the pilot.

## Implementation state

Pending — PXG-079 (data retention & privacy), Sprint 4.
