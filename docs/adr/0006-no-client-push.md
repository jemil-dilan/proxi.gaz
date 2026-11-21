# ADR 0006: No client push notifications in the MVP

- **Status:** Accepted
- **Date:** 2026-11-18
- **Source:** Decision D6 in [`ProxiGaz_Project_Analysis.md` §3](../ProxiGaz_Project_Analysis.md#3-decisions-register)

## Context

Clients are anonymous and a client push channel costs money and setup.

## Decision

The client polls `GET /orders/track/{token}`. `NotificationChannel` serves vendors and the admin only.

## Consequences

Zero cost; the client app must poll. Rules out price-approval flows that need to reach the client (ADR 0003).

## Implementation state

Partly done — `NotificationChannel` port with a logging adapter (PXG-008). Tracking endpoint: Sprint 2.
