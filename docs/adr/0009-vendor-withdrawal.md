# ADR 0009: Vendor withdrawal after acceptance

- **Status:** Accepted
- **Date:** 2026-11-18
- **Source:** Decision D9 in [`ProxiGaz_Project_Analysis.md` §3](../ProxiGaz_Project_Analysis.md#3-decisions-register)

## Context

A vendor can withdraw after accepting an order.

## Decision

Withdrawal moves the order to `ECHOUEE` (terminal) and counts against reliability (ADR 0018). The admin may re-dispatch manually.

## Consequences

Simple, no hidden retry loops; withdrawn orders need admin attention.

## Implementation state

Pending — PXG-075 (vendor withdraw after accept), Sprint 3.
