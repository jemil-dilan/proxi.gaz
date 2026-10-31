# Sprint Releases and Deployment

Every completed sprint produces a staging deployment, release notes, and a version tag. The pilot production environment is updated from an accepted sprint release after approval. For the pre-1.0 API, use `v0.MINOR.PATCH` SemVer tags; prerelease versions such as `v0.1.0-alpha.1` are reserved for limited testing and do not mark a sprint complete.

## Sprint release gate

1. Complete the sprint's implementation and acceptance criteria. The task owner runs the agreed unit, data, and E2E suites.
2. From `main`, manually dispatch `.github/workflows/release.yml` with a version and matching `docs/releases/v<version>.md` notes. The workflow reruns formatting and all test suites before publishing.
3. Build and publish one multi-platform Docker image to GHCR, tagged with the version and source commit. Preserve the image digest from the build.
4. Deploy that exact digest to staging. Apply forward-only Liquibase migrations, then run health and acceptance checks against the deployed application. The deployment script restores the previous application image if smoke checks fail; it does not roll back database migrations.
5. Review and accept the staging deployment through GitHub's protected `release-approval` environment. The approver must verify the sprint acceptance scenarios before approving publication. Only after approval does the workflow create the annotated Git tag and publish the GitHub Release, including the immutable image reference in the release notes.
6. After product acceptance, deploy the same image digest to production. Do not rebuild the image between staging and production.

## Pilot deployment

Docker Compose on a small VPS is the initial pilot deployment approach. The repository includes a local app + PostGIS stack in `compose.yaml`, a staging app definition in `compose.staging.yaml`, a non-root multi-stage `Dockerfile`, and a GitHub release workflow. The staging database must be a separate, persistent PostgreSQL/PostGIS instance; it must not share the disposable application container lifecycle.

### Required GitHub configuration

Create a GitHub Actions environment named `staging` with these environment secrets:

- `STAGING_HOST`: SSH hostname or IP of the staging VPS.
- `STAGING_SSH_USER`: deployment account, with Docker Compose access and write permission to `/opt/proxi-gaz/staging`.
- `STAGING_SSH_PRIVATE_KEY`: private key for that account.
- `STAGING_KNOWN_HOSTS`: pre-verified SSH host key entry for the server.

Create an environment named `release-approval` and configure required reviewers. The workflow waits at this gate after staging smoke checks; the approver must verify sprint acceptance scenarios before approving publication. GitHub's required-reviewer feature depends on repository visibility and plan, so confirm it is available for this repository before relying on the gate.

The workflow publishes to `ghcr.io/<owner>/<repository>` using the workflow's `GITHUB_TOKEN` with `packages: write`. The staging deployment account must be able to pull that package. For a private package, provision a GitHub Packages read token on the host and log Docker in as the deployment user; do not pass the token through the workflow or commit it.

### Required staging host configuration

Provision Docker Engine with Compose v2, `curl`, a TLS reverse proxy, and the `/opt/proxi-gaz/staging` directory owned by the deployment account. Configure `/etc/proxi-gaz/staging.env` outside the repository with mode `0600` and these values:

```dotenv
DB_URL=jdbc:postgresql://<staging-db-host>:5432/proxigaz
DB_USER=<staging-database-user>
DB_PASSWORD=<staging-database-password>
JWT_SECRET=<unique-random-secret-at-least-32-characters>
```

The database must have PostGIS available, persistent storage, off-host backups, and a verified restore procedure before production use. Staging Liquibase uses the `staging` context and excludes local seed data. If bootstrapping an initial admin, enable it once with protected `PROXIGAZ_ADMIN_BOOTSTRAP_*` values, then disable bootstrap and remove those values after confirming the account works.

The app listens on `127.0.0.1:8080` on the host; expose it through the host's TLS reverse proxy. `scripts/deploy-staging.sh` records the active image digest, deploys by digest, runs health/auth smoke checks, and restores the previous image if those checks fail. Database migrations are forward-only, so application-image rollback does not reverse a migration.

## Migration and rollback policy

- Liquibase changesets that have run in a shared environment are immutable. Correct deployed schemas with new forward changesets.
- Take a database backup before production migrations and confirm that it is restorable.
- Prefer application rollback to the previous image digest when the schema remains backward compatible. If a migration is not backward compatible, document a forward-fix procedure; do not assume restoring the application image reverses database changes.
- Keep seed data and local-only configuration out of staging and production.
- Keep `/etc/proxi-gaz/staging.env`, Docker registry credentials, SSH keys, and database backups outside the repository and application image.
