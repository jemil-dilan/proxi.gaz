# ADR 0012: `is_open_now` belongs to the vendor

- **Status:** Accepted
- **Date:** 2026-11-18
- **Source:** Decision D12 in [`ProxiGaz_Project_Analysis.md` §3](../ProxiGaz_Project_Analysis.md#3-decisions-register)

## Context

The spec has a single "Ouvert maintenant" toggle per vendor, while availability is per brand.

## Decision

`is_open_now` is a **Vendor** attribute; `is_available` and `available_until` stay on **VendorBrand**. A vendor's brand is available when `vendor.is_active ∧ vendor.is_open_now ∧ brand.is_available ∧ brand.available_until > :now`.

## Consequences

One toggle closes the whole shop.

## Implementation state

**Not yet matching the schema:** `c_is_open_now` is on `t_vendor_brands` (`v-1-core-schema.xml`). PXG-065 moves it to `t_vendors` with a forward migration. The `Availability` value object (PXG-006) already implements the rule.
