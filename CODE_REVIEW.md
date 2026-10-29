# Code Review — ProxiGaz Backend

**Review date:** 2026-10-29  
**Reviewed branch:** `working/PXG-012B`  
**Review basis:** source, configuration, migration, contract, and documentation inspection. The user reports that unit, data, and E2E suites pass on this worktree; tests and builds were not rerun for this review.

## Summary

The current code has a recognizable hexagonal structure, separates User identity from the Vendor aggregate, uses OpenAPI generation, and persists vendor onboarding through a transactional adapter. The implementation is still in progress. The review found a concrete gap in vendor authentication and several hardening tasks, but some findings from the earlier version of this document were stale or unsupported.

This document does not assign an overall quality score, estimate test coverage, or make a blanket production-readiness decision. Production readiness depends on the intended launch scope and operational configuration. If vendors must authenticate in this release, the session gap below blocks that use case.

## Findings

### P0 — Complete vendor authentication before enabling vendor access

`CreateVendorUseCase` generates a session token, stores its hash through `VendorOnboardingRepository`, and returns the raw token once in the onboarding response. The persistence adapter saves User, Vendor, and session in one transaction. The OpenAPI contract currently defines vendor onboarding and profile update operations, but no operation validates, rotates, or revokes a vendor session.

As a result, the issued token cannot yet be used by a vendor to authenticate subsequent requests. Before vendor access is enabled, define the session lifecycle and add the corresponding application ports/use cases, API contract, persistence behavior, authorization integration, and tests. Decide explicitly on expiration, rotation, and revocation semantics. Do not describe the one-time token response itself as a plaintext-storage flaw: the stored value is a hash, and the raw token is returned to the client for initial use.

Evidence: `CreateVendorUseCase.java`, `JpaVendorOnboardingRepository.java`, `VendorSession*.java`, `specs/openapi/main.yaml`.

### P2 — Avoid a shared JWT secret in any shared local environment

`application-local.yaml` falls back to a shared development JWT secret when `JWT_SECRET` is absent. Staging and production require the environment variable and have a validator, so this is limited to the local profile. Keep the fallback confined to isolated developer machines; require a unique secret if local instances are shared or reachable outside the developer workstation. Never deploy with the local profile.

Evidence: `src/main/resources/application-local.yaml`, `application-staging.yaml`, `application-production.yaml`, `src/main/java/cm/jemil/proxi/gaz/config/NonLocalSecretsValidator.java`.

### P1 — Add explicit REST request validation

The generated API interfaces do include `@Valid` and generated DTOs include `@NotNull` for required fields. However, `build.gradle.kts` declares only `jakarta.validation-api`, not a validation provider, and the contract has few bounds or length constraints. For example, missing required `isStation` or `isActive` values can flow as nullable `Boolean` command fields and be unboxed by the use case, where the generic exception handler returns 500. Add a provider, useful schema constraints, and an E2E assertion for missing required fields. Keep domain invariants as a second line of defense.

Evidence: generated `VendorApi` and `CreateVendorDTO` under `build/generated/sources/openapi/main`, `build.gradle.kts`, `specs/openapi/main.yaml`.

### P2 — Do not silently ignore an update after the target disappears

`JpaVendorRepository.update` uses `findAggregateById(...).ifPresent(...)`. If the vendor is deleted after `UpdateVendorUseCase` loads it but before the adapter's lookup, the update becomes a no-op and the controller still returns `204 No Content`. `JpaUserRepository.update` has the same silent no-op behavior. Preserve the no-optimistic-locking behavior requested for these repositories, but make the adapter report a missing row so callers do not mistake a no-op for a successful update.

Evidence: `UpdateVendorUseCase.java`, `JpaVendorRepository.java`, `VendorRepository.java`.

### P2 — Defensively copy User roles at the aggregate boundary

`User` retains the caller-supplied `Set<UserRole>` directly. Although the getter returns a copy and current factories use immutable sets, `reconstitute` and the public constructor accept mutable sets. A caller retaining that set can change an existing aggregate's roles without going through an explicit domain operation. Copy and validate the set on construction.

Evidence: `src/main/java/cm/jemil/proxi/gaz/domain/user/User.java`.

### P2 — Keep REST wire codes out of the application layer

`GetAllDemoUseCase` and `GetDemoByIdUseCase` import `interfaces.rest.Constants` and switch on REST extraction-code strings. This makes the application layer depend inward on the HTTP adapter and couples its query model to a wire representation. Map the external code to an application-owned projection/query type in the REST mapper or controller.

Evidence: `GetAllDemoUseCase.java`, `GetDemoByIdUseCase.java`, `interfaces/rest/Constants.java`.

### P2 — Add pagination before list data grows

The Demo list operation returns an array and its repository loads the full response set. This is an actual unbounded list path. Add page/size (and any supported sort) to the contract, enforce a maximum page size, and propagate pagination through the inbound port/use case and persistence adapter. There is no evidence in the current API that every resource has an unbounded list endpoint, so scope this change to existing and planned list operations.

Evidence: `/demo` in `specs/openapi/main.yaml`, `DemoRepository` and `JpaDemoRepository`.

### P2 — Tighten email validation to the product's accepted address policy

`Email` uses a simple regex that accepts malformed forms such as consecutive dots in a local part or domain. Decide which address forms the product needs to support, validate to that policy, and add boundary tests. Avoid claiming strict RFC 5322 compliance from a regex; syntactic validation does not establish that an address can receive mail.

Evidence: `src/main/java/cm/jemil/proxi/gaz/domain/shared/Email.java`.

### P2 — Define rate limits for exposed authentication and onboarding operations

No rate limiting was identified in the inspected security configuration. Prioritize login and any future session-creation/refresh operations based on deployment topology and abuse risk. Select an implementation appropriate to the deployment (including shared state if there are multiple application instances), define response behavior, and test it. Spring Security does not provide the specific `rateLimiter` DSL shown in the previous review; that example should not be used.

Evidence: `SecurityConfig.java`, OpenAPI authentication and onboarding operations.

## Findings removed or corrected from the previous review

- **“Two user tables remain in the schema” is stale.** `t_admin_users` is part of the historical v1 schema and is used by the earlier seed migration. The v3 migration copies the admin data into `t_users`, assigns the ADMIN role, then drops `t_admin_users`. The current master changelog includes v3. Verify migration behavior against a real database before release, but do not call the historical table a final-schema defect.
- **“Duplicate admin seed data” is unproven.** The local seed inserts a known admin row before the migration; the bootstrap runner checks for an existing email before inserting. A duplicate requires a concrete conflicting configuration or data case. Keep bootstrap credentials controlled and verify seed/bootstrap behavior in the configured environments.
- **“The use case has no transaction” is inaccurate.** `JpaVendorOnboardingRepository.insert` is transactional and saves all three records inside that transaction. Transaction ownership at the persistence adapter is consistent with the current atomic persistence port.
- **“VendorRepository needs a create method” is not a demonstrated defect.** New Vendor creation deliberately uses the onboarding port to persist User, Vendor, and session atomically; the separate Vendor repository handles lookup and update.
- **`Vendor.replaceDetails` already rejects null** with `Objects.requireNonNull`.
- **Returning the raw onboarding token once is part of credential delivery.** It is marked `no-store` at the REST response and the database stores its hash. The missing work is token lifecycle support, not avoiding delivery to the client.
- **Test coverage percentages, vulnerability counts, quality scores, and a 2–3 week estimate were not measured.** Remove them unless backed by actual reports and an explicit estimation method.

## Other recommendations: validate against requirements before scheduling

These may be useful but are not automatically blockers based only on this repository review:

- Password complexity policy: first define whether vendors will use passwords; the current onboarding direction uses a generated session credential.
- Token revocation: decide the session lifecycle as part of the P0 authentication work.
- Request/response logging, correlation IDs, metrics, tracing, and compression: prioritize from operational and privacy requirements; never log credentials or raw session tokens.
- Caching: add only for measured read paths with an explicit invalidation policy.
- API versioning: use a versioning strategy when compatibility requirements call for it.
- Dependency upgrades and build caching: check the dependency catalog and CI constraints before recommending particular versions or tools.
- Database pool sizing and TLS: set from expected deployment load and database topology; validate production connection security in deployment configuration.

## Review limits and next review

This is a static review of the current working tree. Test success is reported by the user, not independently verified here. Migration execution, dependency vulnerability scans, deployment manifests, and production infrastructure were not verified. The working tree contains staged, unstaged, and untracked changes, so review the complete diff before merging.

Recommended next work: resolve the update no-op and User role-copy findings, decide and implement the vendor session lifecycle before vendor access, and complete request-validation runtime support with a missing-required-field acceptance case. Review staging deployment configuration before release.
