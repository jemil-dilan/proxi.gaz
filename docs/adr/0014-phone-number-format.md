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

**Partly done** — the contract validates vendor phones on create and update (`CreateVendorDTO` / `VendorUpdateDTO`: `^\+237[26]\d{8}$`), so only E.164 input is accepted at the API boundary. Not done: the `PhoneNumber` domain value object still accepts any non-blank value of at most 32 characters, and local input formats are not normalised (*amended 2026-11-18, PXG-063*). Client phones: PXG-022.
