# ADR 0007: Confirmation code handling

- **Status:** Accepted
- **Date:** 2026-11-18
- **Source:** Decision D7 in [`ProxiGaz_Project_Analysis.md` §3](../ProxiGaz_Project_Analysis.md#3-decisions-register)

## Context

The client must be able to show the delivery confirmation code; the vendor must not be able to read it.

## Decision

Generated at order creation and returned to the client (create response, and tracking while `CONFIRMEE`/`EN_COURS`). **Never** sent to vendor endpoints. Stored in clear because the client must display it; protection is 3 attempts and a lock (ADR 0008).

## Consequences

A database leak exposes codes of live orders; accepted given the attempt limit and short lifetime.

## Implementation state

Partly done — `ConfirmationCode` value object (PXG-006). Flow: Sprint 2/3.
