# ADR 0021: Keep the Demo slice

- **Status:** Accepted
- **Date:** 2026-11-18
- **Source:** Owner decision, 2026-11-17

## Context

PXG-054 (template leftovers) as written would delete the Demo vertical slice. The slice is the architecture reference that the guideline points to.

## Decision

The Demo slice stays. PXG-054 removes other template leftovers only.

## Consequences

Some non-product code remains in the build and the contract (`/demo`, the `oAuth` scheme).

## Implementation state

**In force** — overrides the Demo part of PXG-054.
