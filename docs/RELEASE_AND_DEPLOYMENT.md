# Sprint Releases and Deployment

Every completed sprint produces a staging deployment, release notes, and a version tag. The pilot production environment is updated from an accepted sprint release after approval.

## Sprint release gate

1. Complete the sprint's implementation and acceptance criteria. The task owner runs the agreed unit, data, and E2E suites.
2. Build one versioned Docker image from the release commit and publish it to the configured container registry.
3. Deploy that exact image to staging. Apply forward-only Liquibase migrations, then run health checks and the sprint's smoke/acceptance scenarios against the deployed application.
4. Write release notes covering capabilities, API or database changes, migration impact, known limitations, and rollback steps.
5. Create an annotated Git tag using Semantic Versioning (`v0.MINOR.PATCH` while the API is pre-1.0) and publish the corresponding release. Tag the image with the same version and retain its immutable digest.
6. After product acceptance, deploy the same image digest to production. Do not rebuild the image between staging and production.

## Pilot deployment

Docker is the application packaging format; it is not by itself a deployment target. Docker Compose on a small VPS is the initial pilot deployment approach. Keep secrets outside the image and provide them through the host's protected environment/secrets mechanism. Persist database data, maintain off-host backups, and verify a restore procedure before production use. The database may run as a managed PostgreSQL service with PostGIS or in a separately managed container with persistent storage; it must not share the disposable application container lifecycle.

The repository does not yet contain a production Dockerfile, application Compose deployment, image registry workflow, or deployment automation. PXG-012C will implement the container build/publish path and staging deployment workflow. Until then, sprint releases require a documented manual deployment using the same image and checks.

## Migration and rollback policy

- Liquibase changesets that have run in a shared environment are immutable. Correct deployed schemas with new forward changesets.
- Take a database backup before production migrations and confirm that it is restorable.
- Prefer application rollback to the previous image digest when the schema remains backward compatible. If a migration is not backward compatible, document a forward-fix procedure; do not assume restoring the application image reverses database changes.
- Keep seed data and local-only configuration out of staging and production.
