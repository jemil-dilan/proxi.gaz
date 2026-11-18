# ADR 0008: Locked confirmation code after 3 wrong attempts

- **Status:** Accepted
- **Date:** 2026-11-18
- **Source:** Decision D8 in [`ProxiGaz_Project_Analysis.md` §3](../ProxiGaz_Project_Analysis.md#3-decisions-register)

## Context

A vendor may have delivered and simply mistyped the code.

## Decision

After 3 wrong codes the code is **locked**, the order stays `EN_COURS` and appears in a "needs attention" list. The admin resolves it (force `LIVREE` or `ECHOUEE`), and the resolution is audited.

## Consequences

No automatic failure on typos; needs an admin action and an audited override.

## Implementation state

Pending — Sprint 3. Audit trail available since PXG-061 (ADR 0023).
