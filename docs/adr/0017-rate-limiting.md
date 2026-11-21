# ADR 0017: Rate limiting

- **Status:** Accepted
- **Date:** 2026-11-18
- **Source:** Decision D17 in [`ProxiGaz_Project_Analysis.md` §3](../ProxiGaz_Project_Analysis.md#3-decisions-register)

## Context

The pilot runs as a single free-tier instance without Redis.

## Decision

Rate limiting goes through a `RateLimiter` port backed by an in-memory token bucket. `X-Forwarded-For` is trusted only from the configured proxy.

## Consequences

Limits reset on restart and are per instance; acceptable for a single-instance pilot.

## Implementation state

**Done** for admin login (PXG-059: `InMemoryTokenBucketRateLimiter`, email+IP lockout). **Amendment (2026-11-18):** no proxy is configured yet, so the client IP is the direct remote address and `X-Forwarded-For` is ignored; add the trusted-proxy setting when a reverse proxy is deployed.
