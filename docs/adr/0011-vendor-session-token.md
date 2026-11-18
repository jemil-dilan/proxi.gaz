# ADR 0011: Vendor session token

- **Status:** Accepted
- **Date:** 2026-11-18
- **Source:** Decision D11 in [`ProxiGaz_Project_Analysis.md` §3](../ProxiGaz_Project_Analysis.md#3-decisions-register)

## Context

Vendors need long sessions on their phones (spec requirement), so the token must be strong and revocable.

## Decision

At least 256 random bits, stored as a SHA-256 hash, `last_used_at` tracked, no expiry but revocable and rotatable. A valid token for an **inactive** vendor returns `403 PROXIGAZ-403-002`.

**Amendment (2026-11-18):** no lookup prefix. Tokens are looked up by their full SHA-256 hash, which has a unique index; a prefix adds nothing at this scale.

## Consequences

Raw tokens are shown once (creation and rotation responses use `Cache-Control: no-store`).

## Implementation state

**Done** — PXG-012B, PXG-015, PXG-058 (`SecureVendorSessionTokenGenerator`: 32 random bytes; soft revoke/rotate; `last_used_at` throttled to once a minute).
