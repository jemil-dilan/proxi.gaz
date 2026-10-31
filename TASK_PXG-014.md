# Task PXG-014: Paginated vendor listing and filters

Plan approved: 2026-10-29
Use case(s): VEN-01
Branch: working/PXG-014

## Steps
- [x] Add acceptance coverage for pagination, active-status filtering, name search, and admin authorization.
- [x] Add an application-owned search criteria and page result model.
- [x] Extend the VendorRepository port and JPA adapter with stable paginated filtering.
- [x] Add the admin list API, bounded query parameters, and response metadata to OpenAPI.
- [x] Map page results to vendor list DTOs without exposing persistence entities.
- [x] Add use-case and persistence coverage for combined filters and page boundaries.
- [x] Regenerate OpenAPI API/DTO sources.
- [x] Update sprint status and API documentation.
- [ ] Run unit, data, and E2E suites after authorization.
- [ ] Review the diff and remove this task file after completion.
