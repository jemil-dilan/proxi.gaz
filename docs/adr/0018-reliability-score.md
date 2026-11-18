# ADR 0018: Vendor reliability score

- **Status:** Accepted
- **Date:** 2026-11-18
- **Source:** Decision D18 in [`ProxiGaz_Project_Analysis.md` §3](../ProxiGaz_Project_Analysis.md#3-decisions-register)

## Context

Fallback ranking (ADR 0004) and the admin need a simple, explainable reliability measure.

## Decision

Rolling 30 days: `100 × (1 − (0.5·refused + 1.0·expired + 2.0·withdrawn) / offers)`, clamped to [0, 100]; `NULL` below 5 offers. Weights are configurable. Visible to the admin only.

## Consequences

New vendors are neutral until they have 5 offers.

## Implementation state

Pending — PXG-019, Sprint 3 (needs order data).
