# ADR 0022: UnitOfWork port as the use-case transaction boundary

- **Status:** Accepted
- **Date:** 2026-11-18
- **Source:** PXG-061

## Context

Repository adapters were each `@Transactional`, so a use case touching two adapters ran in two transactions. The audit log needs the business change and its audit row to commit or roll back together.

## Decision

Application code opens a transaction through the `UnitOfWork` port (`inTransaction(Supplier)`). `SpringUnitOfWork` implements it with a `TransactionTemplate`; adapters keep `@Transactional` (REQUIRED) and join it. Ports that must never write alone use `Propagation.MANDATORY`.

## Consequences

No Spring in the application layer. Unit tests stub the port with Mockito; data tests cover real rollback.

## Implementation state

**Done** — PXG-061 (vendor create/update/status/session rotate/revoke). New multi-step write use cases must use it, e.g. PXG-022 order creation.
