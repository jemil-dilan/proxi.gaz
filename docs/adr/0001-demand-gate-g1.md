# ADR 0001: Demand gate G1 before Sprint 2a

- **Status:** Accepted
- **Date:** 2026-11-18
- **Source:** Decision D1 in [`ProxiGaz_Project_Analysis.md` §3](../ProxiGaz_Project_Analysis.md#3-decisions-register)

## Context

The Cadrage requires proof of demand before real investment. Sprints 0–1 are low-regret; Sprint 2 (order flow) is the first large investment.

## Decision

Gate **G1**: manual-test numbers that meet the Cadrage thresholds, **or** a dated written waiver with rationale recorded in `status.md`, are required before PXG-022 and everything after it.

## Consequences

Sprint 2a cannot start silently. A waiver keeps the decision visible and reversible.

## Implementation state

**Satisfied by waiver** on 2026-11-18 (product owner), recorded in `status.md` → Gates: build the MVP first and collect pilot numbers in parallel to settle the open product decisions.
