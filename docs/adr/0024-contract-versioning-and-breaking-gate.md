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
