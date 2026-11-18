# ADR 0013: Time handling

- **Status:** Accepted
- **Date:** 2026-11-18
- **Source:** Decision D13 in [`ProxiGaz_Project_Analysis.md` §3](../ProxiGaz_Project_Analysis.md#3-decisions-register)

## Context

Tests need control of time, and the API serves clients in Africa/Douala.

## Decision

All timestamps are `TIMESTAMPTZ` in UTC. The API uses ISO-8601 UTC. Display in Africa/Douala is a frontend concern. SQL receives `:now` from the `Clock` port and never calls `now()` in business queries.

## Consequences

`FakeClock` drives time-based tests without touching the database.

## Implementation state

**Partly done** — `TIMESTAMPTZ` everywhere and the `Clock` port with `SystemClock`/`FakeClock` (PXG-008). **Gap:** the generator uses `java8-localdatetime`, so API date-times are UTC but sent without an offset (`2026-11-18T10:15:30`, no `Z`). Switching to `OffsetDateTime` changes the wire format; schedule it before contract milestone C1.
