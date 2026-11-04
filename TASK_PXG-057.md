# Task PXG-057: Vendor DTO & Validation Fixes

Plan approved: 2026-11-03
Use case(s): VEN-01 (Vendor Management)
Branch: working/PXG-057

## Steps
- [x] Update OpenAPI contract to unify `address` → `addressText` in CreateVendorDTO
- [x] Remove `isActive` from required in CreateVendorDTO and VendorUpdateDTO
- [x] Add `addressText` to CreateVendorDTO
- [x] Add enum for `deliveryFeeType` (NONE | FIXED)
- [x] Add phone validation pattern `^\+237[26]\d{8}$` to phone fields
- [x] Add Cameroon bounding box validation to coordinates
- [x] Add `createdAt` and `updatedAt` fields to VendorDTO
- [x] Add `email` (nullable) field to VendorDTO
- [x] Add `Location` header response documentation for 201
- [x] Regenerate DTOs from updated OpenAPI contract
- [x] Update RestMapper to handle new fields
- [x] Update VendorJpa entity with updatedAt column
- [x] Create migration for updatedAt column
- [x] Update JpaMapper to map new fields
- [x] Fix E2E test features and step definitions
- [x] Run all tests to verify no regressions
