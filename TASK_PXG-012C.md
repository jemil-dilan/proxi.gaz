# Task PXG-012C: Docker image and sprint release automation

Plan approved: 2026-10-31 (user approved proceeding with the release guideline)  
Use case(s): Delivery / release infrastructure  
Branch: `working/PXG-012C`

## Steps

- [x] Add a version-aware, multi-stage Dockerfile that runs as a non-root user and exposes an application health check.
- [x] Add a local Docker Compose stack for the application and PostGIS, with health-gated startup and persistent local database storage.
- [x] Add a staging Compose definition that uses a protected external staging environment file and loopback-only app binding.
- [x] Add a staging deployment script that deploys the published image by immutable digest, checks health, and restores the previous image if the app health check fails.
- [x] Add a manually dispatched GitHub Actions workflow that validates the selected main commit, runs unit/data/E2E tests, publishes version and SHA image tags to GHCR, deploys the image digest to staging, and creates an annotated Git tag/GitHub Release only after staging acceptance approval.
- [x] Document GHCR, GitHub environment, SSH host, environment secret, backup, deployment, rollback, and alpha release setup.
- [x] Update README and status to describe the actual container and release workflow and correct stale PXG-013–015 status.
- [x] Validate Compose configuration, build and locally smoke-test the container, and run the project verification suites.
- [x] Confirm release workflow and deployment acceptance criteria; update status, remove this temporary checklist, and commit through Commitizen.
