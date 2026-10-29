# Task PXG-015: Vendor session lifecycle and PXG-012B review follow-ups

Plan approved: 2026-10-29
Use case(s): AUTH-02, VEN-01
Branch: working/PXG-015

## Steps
- [x] Define a long-lived vendor session as active until revoked; rotation revokes the prior token and returns the new token once.
- [x] Add vendor-session lookup, validation, rotation, and revocation application ports/use cases.
- [x] Add persistence behavior and admin/vendor API authorization for the session lifecycle.
- [x] Extend the OpenAPI contract and generated API types for session operations and effective vendor request constraints.
- [x] Add a Bean Validation runtime provider and map invalid request bodies to clear 400 responses.
- [x] Make vendor update persistence report a missing row instead of silently succeeding.
- [x] Defensively copy and validate User roles at construction.
- [x] Add focused unit, persistence, security, and API acceptance coverage for the changes.
- [x] Update status and relevant API/runtime documentation.
- [ ] Run unit, data, and E2E suites; pending explicit user request.
- [ ] Review the complete diff and remove this task file when implementation is complete.
