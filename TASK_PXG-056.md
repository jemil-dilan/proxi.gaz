# Task PXG-056: Security Schemes & Audience Separation

Plan approved: 2026-11-03 by user confirmation
Use case(s): US-1.0.1 (A trustworthy contract)
Branch: working/PXG-056

## Steps
- [ ] Add security schemes to OpenAPI contract (adminAuth, vendorSessionAuth)
- [ ] Apply adminAuth to /admin/** routes
- [ ] Apply vendorSessionAuth to /vendor/** routes
- [ ] Ensure public routes have security: []
- [ ] JWT carries audience claim (admin)
- [ ] Vendor guard rejects JWTs with 401/403
- [ ] Admin guard rejects opaque tokens with 401/403
- [ ] Write E2E tests for audience separation
- [ ] Run full test suite
- [ ] Confirm all acceptance criteria from US-1.0.1 are met
