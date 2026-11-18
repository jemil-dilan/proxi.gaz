# ADR 0003: Fallback price rule B

- **Status:** Accepted
- **Date:** 2026-11-18
- **Source:** Decision D3 in [`ProxiGaz_Project_Analysis.md` §3](../ProxiGaz_Project_Analysis.md#3-decisions-register)

## Context

When the first vendor does not accept, the order falls back to another vendor whose price may differ. Option A (ask the client to approve a higher price) needs a client notification channel that does not exist (ADR 0006).

## Decision

Automatic fallback only to offers with `total ≤ quoted_total`. A higher price only through an admin override after a phone call with the client. Count the candidates excluded by price (`FALLBACK_PRICE_EXCLUDED`).

## Consequences

The client never pays more than quoted without a human conversation. Some fallbacks find no candidate; the counter shows how often.

## Implementation state

Pending — fallback, Sprint 3.
