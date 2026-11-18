# ADR 0025: Logging policy

- **Status:** Accepted
- **Date:** 2026-11-18
- **Source:** PXG-061

## Context

Debugging needs SQL and a way to follow one request; SQL bind values contain password hashes, token hashes and phone numbers.

## Decision

Every log line carries the request correlation ID (MDC), also returned as `X-Correlation-Id`. 4xx responses log at WARN, 5xx at ERROR. Slow queries are logged in every environment. SQL statements are logged on `local` and `staging`; **bind values only on `local`**, enforced by `LoggingConfigurationTest`.

## Consequences

Production debugging raises loggers through environment variables, never bind values.

## Implementation state

**Done** — PXG-061; see `docs/RUNTIME.md` → Logging.
