# ADR 0014: Phone number format

- **Status:** Accepted
- **Date:** 2026-11-18
- **Source:** Decision D14 in [`ProxiGaz_Project_Analysis.md` §3](../ProxiGaz_Project_Analysis.md#3-decisions-register)

## Context

Limits, the block list and support all need one phone format.

## Decision

Store E.164: `+237` and 9 digits (`^\+237[26]\d{8}$`). Accept local input formats and normalise at the edge.

## Consequences

Duplicate detection and rate limits work on one canonical value.

## Implementation state

**Pending** — `PhoneNumber` currently accepts any non-blank value of at most 32 characters. Normalisation and validation: vendor phones before the pilot, client phones with PXG-022.
