# ADR 0024: Contract versioning and breaking-change gate

- **Status:** Accepted
- **Date:** 2026-11-18
- **Source:** PXG-060

## Context

Frontend work runs in parallel against the OpenAPI contract, so contract breaks must be deliberate and visible.

## Decision

`specs/openapi/main.yaml` keeps `info.version: 0.1.0-dev`; the release workflow stamps the tag version into a published `openapi-<version>.yaml`. CI lints with Spectral and runs `oasdiff breaking` against the target branch; a break fails the PR unless it is labelled `contract-breaking-approved`.

## Consequences

Breaking changes need an explicit label and a release-notes entry.

## Implementation state

**Done** — PXG-060.

**Amendment (2026-11-24, PXG-064):** response values that are meant to grow — the audit log's `action` and `targetType` — are declared as strings with `x-extensible-enum` instead of `enum`. oasdiff treats adding a value to a response `enum` as breaking, while adding to `x-extensible-enum` is not; clients must therefore accept values they do not know. The change itself was breaking once (PR labelled `contract-breaking-approved`). Request filters keep strict enums (unknown values get 400). Use `x-extensible-enum` for any future response field whose value set grows with features.
