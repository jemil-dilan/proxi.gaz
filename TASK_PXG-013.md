# Task PXG-013: Admin vendor activation and deactivation

Plan approved: 2026-10-29
Use case(s): VEN-02
Branch: working/PXG-013

## Steps
- [x] Add acceptance coverage for admin activation/deactivation, authorization, missing vendors, and profile updates preserving status.
- [x] Add an idempotent admin status endpoint with an explicit desired `isActive` value.
- [x] Remove `isActive` from the vendor profile update contract and command mapping; keep initial status on onboarding.
- [x] Add domain activation/deactivation behavior and an application use case.
- [x] Add unit coverage for state changes and persistence behavior.
- [x] Regenerate OpenAPI API/DTO sources.
- [x] Update sprint status to record PXG-013 implementation and PXG-020 nearby-search filtering dependency.
- [ ] Run unit, data, and E2E suites; pending explicit user request.
- [ ] Run the agreed unit, data, and E2E suites after authorization.
- [ ] Review the diff and remove this task file after completion.
