# ADR 0016: Contact details revealed only after acceptance

- **Status:** Accepted
- **Date:** 2026-11-18
- **Source:** Decision D16 in [`ProxiGaz_Project_Analysis.md` §3](../ProxiGaz_Project_Analysis.md#3-decisions-register)

## Context

Privacy, and preventing clients and vendors from bypassing the platform before confirmation.

## Decision

The public nearby search does **not** expose the vendor phone. The vendor phone (to the client) and the client's exact coordinates and phone (to the vendor) are revealed only **after acceptance**.

## Consequences

Responses differ before and after acceptance; tests must check both.

## Implementation state

Pending — PXG-066 (nearby), Sprint 2 (offers).
