# Sprint Releases and Deployment

Every completed sprint produces a staging deployment, release notes, and a version tag. The pilot production environment is updated from an accepted sprint release after approval. For the pre-1.0 API, use `v0.MINOR.PATCH` SemVer tags; prerelease versions such as `v0.1.0-alpha.1` are reserved for limited testing and do not mark a sprint complete.

## Sprint release gate

1. Complete the sprint's implementation and acceptance criteria. The task owner runs the agreed unit, data, and E2E suites.
2. From `main`, manually dispatch `.github/workflows/release.yml` with a version and matching `docs/releases/v<version>.md` notes. The `verify` job reruns formatting, all test suites and the contract checks, and prepares the versioned OpenAPI file.
3. The `publish-image` job builds the image with **Jib** (no Dockerfile; base `eclipse-temurin:25-jre`, non-root user `10001`) and publishes it to Docker Hub (`docker.io/mohamednfonka/proxi.gaz`) and GHCR, tagged with the version and `latest`. It resolves each image's immutable digest and appends the tags and digests to the release notes; deploy the digest.
4. **Deploy that exact digest to staging** and run the sprint acceptance scenarios against it. This step is **manual and currently has no target**: no staging host is provisioned (see Pilot deployment).
5. The `publish-release` job waits for approval in the protected `release-approval` environment. The approver confirms the acceptance scenarios passed, then the job creates the annotated Git tag and the GitHub Release with the OpenAPI file attached.
6. After product acceptance, deploy the same image digest to production. Do not rebuild the image between staging and production.

## API contract versioning

`specs/openapi/main.yaml` is the contract of record. Its `info.version` stays at the development marker `0.1.0-dev`; the repository never claims an unreleased version.

- **Every PR and push to `main`** runs the `OpenAPI Contract` CI job: Spectral lint (`scripts/validate-openapi.sh`, rules in `.spectral.yaml`) and an oasdiff breaking-change check of the PR's contract against the target branch (`scripts/check-openapi-breaking.sh`). Removing a required response field, removing an endpoint, or adding a required request field fails the job.
- **Intentional breaking changes** (allowed before 1.0) need the PR label `contract-breaking-approved`; add it, then re-run the job. Record the change under "API and database impact" in the sprint's release notes. On `main` the check only warns, because the change was gated at review.
- **Every release** generates `openapi-<version>.yaml` with `scripts/prepare_openapi_release.py`: `info.version` equals the release version (the tag without `v`), and the repository variable `STAGING_API_URL` (https) is added to `servers` when set. The file is linted, uploaded as a workflow artifact, and attached to the GitHub Release. Clients generate code from that file, not from `main`.

## Pilot deployment

The release workflow publishes images only; it does not deploy. The earlier VPS staging setup (`Dockerfile`, `compose.staging.yaml`, `scripts/deploy-staging.sh`, `scripts/staging-smoke.sh`) was removed in PXG-012C (commit `2e2ca19`) when image builds moved to Jib. `compose.yaml` remains for the local app + PostGIS stack.

**Open:** no staging environment exists yet, so step 4 of the release gate cannot be performed. Until one is chosen, a sprint release is not complete under this policy. Whatever host is chosen must provide:

- a separate, persistent PostgreSQL/PostGIS database with off-host backups and a verified restore procedure (never tied to the application container's lifecycle);
- the `staging` Spring profile with `DB_URL`, `DB_USER`, `DB_PASSWORD` and a unique `JWT_SECRET` (at least 32 characters) supplied as protected environment values, never committed;
- TLS in front of the application, and a way to deploy a specific image digest and roll back to the previous one;
- the repository variable `STAGING_API_URL` set to its https URL, so release contracts list the staging server.

Staging Liquibase uses the `staging` context and excludes local seed data. To create the first admin, enable `PROXIGAZ_ADMIN_BOOTSTRAP_*` once with protected values, confirm the account works, then disable bootstrap and remove those values.

### Required GitHub configuration

- Repository secrets `DOCKERHUB_USERNAME` and `DOCKERHUB_TOKEN` for the Docker Hub push. GHCR uses the workflow's `GITHUB_TOKEN` with `packages: write`.
- An environment named `release-approval` with required reviewers. GitHub's required-reviewer feature depends on repository visibility and plan; confirm it is available before relying on the gate.
- Optional repository variable `STAGING_API_URL` (https), added to the release contract's `servers`.

## Migration and rollback policy

- Liquibase changesets that have run in a shared environment are immutable. Correct deployed schemas with new forward changesets.
- Take a database backup before production migrations and confirm that it is restorable.
- Prefer application rollback to the previous image digest when the schema remains backward compatible. If a migration is not backward compatible, document a forward-fix procedure; do not assume restoring the application image reverses database changes.
- Keep seed data and local-only configuration out of staging and production.
- `t_admin_audit_log` (PXG-061, `v-7-admin-audit-log.xml`) is append-only and has no rollback changeset: dropping it destroys the audit trail. Its foreign key to `t_users` blocks deleting an admin who has audit entries.
- Keep environment secrets, Docker registry credentials, and database backups outside the repository and the application image.
