# ADR 0015: Error model: RFC 7807 problem details

- **Status:** Accepted
- **Date:** 2026-11-18
- **Source:** Decision D15 in [`ProxiGaz_Project_Analysis.md` §3](../ProxiGaz_Project_Analysis.md#3-decisions-register)

## Context

Clients need one predictable error format with stable codes.

## Decision

Errors are `application/problem+json` with `code` = `PROXIGAZ-{HTTP}-{NNN}`, `correlationId`, and an optional `errors[]`.

## Consequences

Codes are part of the contract and listed in `docs/ERROR_CODES.md`.

## Implementation state

**Done** — PXG-055; correlation ID on every log line since PXG-061 (ADR 0025).
