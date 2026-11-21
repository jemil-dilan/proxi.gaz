# ADR 0010: Tracking token and public reference

- **Status:** Accepted
- **Date:** 2026-11-18
- **Source:** Decision D10 in [`ProxiGaz_Project_Analysis.md` §3](../ProxiGaz_Project_Analysis.md#3-decisions-register)

## Context

Tracking links must not be guessable, and a database leak must not expose them. Support needs a short reference for phone and WhatsApp.

## Decision

`tracking_token`: at least 128 random bits, stored as a SHA-256 hash; the raw value is returned once. Each order also has a short `public_ref` (Crockford base32, e.g. `PG-7K3M9`).

## Consequences

A lost tracking link cannot be recovered from the database; support uses `public_ref`.

## Implementation state

Partly done — `TrackingToken` value object (PXG-006). Order creation: PXG-022.
