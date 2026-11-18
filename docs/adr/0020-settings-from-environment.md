# ADR 0020: Settings come from environment configuration

- **Status:** Accepted
- **Date:** 2026-11-18
- **Source:** Decision D20 in [`ProxiGaz_Project_Analysis.md` §3](../ProxiGaz_Project_Analysis.md#3-decisions-register)

## Context

A settings CRUD is not worth building for the pilot.

## Decision

Radius, TTL, attempts and escalation delay come from **environment configuration** (`proxigaz.*`, bound by `Configurations`). `GET /admin/settings` exposes them read-only.

## Consequences

Changing a setting needs a restart.

## Implementation state

Partly done — the `Configurations` port and `proxigaz.*` binding (PXG-004). `GET /admin/settings`: PXG-020 (Sprint 1, with the search radius settings).
