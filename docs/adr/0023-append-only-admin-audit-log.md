# ADR 0023: Append-only admin audit trail

- **Status:** Accepted
- **Date:** 2026-11-18
- **Source:** PXG-061

## Context

Admin actions on vendors must be traceable (Analysis §4.3, `t_admin_audit_log`) without leaking secrets.

## Decision

Every admin write records an `AuditEntry` (admin, action, target, metadata, time) in the same transaction (ADR 0022). Rows are never updated or deleted. Metadata accepts only allowlisted keys and types: never tokens, hashes or field values (updates list changed field names only). No-op actions are recorded too. `GET /admin/audit-log` lists entries newest first.

## Consequences

An admin with audit entries cannot be deleted (foreign key). Each new admin write must add its action and metadata keys.

## Implementation state

**Done** — PXG-061 (vendor actions). PXG-064 added the first non-vendor target: `BRAND` with `BRAND_CREATED` (metadata `isActive`). The audit-log response lists actions and target types as extensible values (ADR 0024 amendment), so new ones are not contract breaks.
