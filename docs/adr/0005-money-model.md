# ADR 0005: Money model on orders and offers

- **Status:** Accepted
- **Date:** 2026-11-18
- **Source:** Decision D5 in [`ProxiGaz_Project_Analysis.md` §3](../ProxiGaz_Project_Analysis.md#3-decisions-register)

## Context

Prices must stay consistent between the quote, each offer and the final order, and the pilot KPIs need the figures.

## Decision

Order: `quoted_total`, and `final_total` set at acceptance (always ≤ `quoted_total`). Offer: `unit_price_snapshot`, `delivery_fee_snapshot`, `total_snapshot`. Amounts are whole FCFA (`Money`).

## Consequences

Later price changes never alter a placed order; every offer keeps the price it was made at.

## Implementation state

Partly done — `Money` value object (PXG-006). Order/offer columns: PXG-068.
