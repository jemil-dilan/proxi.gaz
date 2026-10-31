# Action Items - ProxiGaz Backend Code Review

**Source**: [`CODE_REVIEW.md`](CODE_REVIEW.md), current review (2026-10-29)  
**Status (2026-10-31)**: PXG-013 and PXG-014 are merged to `main`. PXG-015 is implemented on `working/PXG-015`; code and test sources compile, but test suites have not been executed and the branch is pending merge.

---

## Priority Legend

- **P0 - BLOCKER**: Must fix before vendors can use the system
- **P1 - HIGH**: Should fix before next deployment
- **P2 - MEDIUM**: Nice to have, implement when capacity allows
- **P3 - LOW**: Future improvements

---

## P0 - BLOCKER (Vendor Authentication Required)

### 🔴 Item #1: Complete Vendor Session Authentication

**Problem**: Vendor onboarding creates a session token; vendors need to authenticate with it and administrators need to manage it. PXG-015 implements session authentication and management, pending merge.

**Impact**: Vendors cannot authenticate or access vendor-specific endpoints.

**Files to Create/Modify**:
- `specs/openapi/main.yaml` - Add vendor session endpoints
- `src/main/java/cm/jemil/proxi/gaz/application/outbound/VendorSessionRepository.java` - Add findByTokenHash method
- `src/main/java/cm/jemil/proxi/gaz/application/usecase/ValidateVendorSessionUseCase.java` - New use case
- `src/main/java/cm/jemil/proxi/gaz/config/VendorSessionAuthenticationFilter.java` - Bearer validation and authorization
- `src/main/java/cm/jemil/proxi/gaz/interfaces/rest/VendorController.java` - Vendor identity and admin session operations

**Acceptance Criteria**:
- [x] GET `/vendor/session` validates the bearer token and returns vendor ID
- [x] Token lookup hashes the presented token and compares it with the stored hash
- [x] Admin rotation replaces the prior token; the previous token is invalidated
- [x] Admin can revoke the active vendor token
- [x] Vendor session endpoint requires a valid vendor bearer token
- [ ] Unit, persistence, and E2E suites executed successfully

**Implementation status**: Acceptance criteria are implemented on `working/PXG-015`; source and test compilation succeeded. The test suites have not been run. Merge PXG-015 after review and the agreed verification gate.

**Effort**: 2-3 days  
**Priority**: P0 - BLOCKER  
**Assignee**: Backend Team  

---

## Security & API Correctness (priority is assigned per item)

### 🟡 Item #2: Confine the local JWT secret fallback

**Problem**: `application-local.yaml` has a shared development JWT secret fallback. Staging and production already require `JWT_SECRET` and reject weak or placeholder values.

**Impact**: Risk applies if a local-profile instance is shared or reachable outside an isolated developer machine. Never deploy with the local profile.

**Fix**:
Keep the current fallback limited to isolated local development, or require a unique `JWT_SECRET` in shared local environments.

**Acceptance Criteria**:
- [ ] Staging/production fail to start if JWT_SECRET is not provided (already enforced)
- [ ] Shared local environments use a unique secret
- [ ] Documentation states the local fallback is development-only and shared local environments need a unique secret

**Effort**: 1 hour  
**Priority**: P2 - MEDIUM  
**Assignee**: DevOps/Backend  

---

### 🟡 Item #3: Add Bean Validation

**Status**: Implemented in PXG-015 (runtime provider and request bounds); test suites have not been executed.

**Problem**: Generated API interfaces already use `@Valid` and DTOs have `@NotNull` for required fields, but the build declared only `jakarta.validation-api` and no runtime validation provider. The OpenAPI schemas also lacked useful length/range constraints, so API-boundary enforcement was incomplete.

**Impact**: Invalid requests may reach domain layer with unclear error messages.

**Files to Modify**:
- `build.gradle.kts` - Add a Jakarta Bean Validation provider compatible with Spring Boot 4
- `specs/openapi/main.yaml` - Add appropriate length, range, and format constraints
- Generated API/DTO sources - Regenerate and confirm validation annotations are present

**Acceptance Criteria**:
- [ ] All DTO fields have appropriate validation annotations
- [ ] Runtime rejects invalid request bodies with 400 responses
- [ ] Validation errors return 400 with clear field-level messages
- [ ] Existing tests updated to handle validation exceptions

**Effort**: 2-3 days  
**Priority**: P1 - HIGH  
**Assignee**: Backend Team  

---

## P2 - MEDIUM (Scalability & Quality)

### 🟢 Item #4: Add Pagination to List Endpoints

**Status**: Vendor listing pagination was implemented in PXG-014 and merged to `main`. `GET /demo` remains unpaginated.

**Problem**: `GET /demo` and other unpaged list endpoints return all records.

**Impact**: Performance and memory issues as data grows.

**Files to Modify**:
- `src/main/java/cm/jemil/proxi/gaz/application/outbound/DemoRepository.java` - Add Page-based method
- `src/main/java/cm/jemil/proxi/gaz/infrastructure/persistence/JpaDemoRepository.java` - Implement pagination
- `src/main/java/cm/jemil/proxi/gaz/application/usecase/GetAllDemoUseCase.java` - Accept Pageable
- `src/main/java/cm/jemil/proxi/gaz/interfaces/rest/DemoController.java` - Add page/size parameters

**Acceptance Criteria**:
- [ ] `GET /demo` supports `?page=0&size=20` parameters
- [ ] Default page size is 20
- [ ] Maximum page size is configurable (e.g., 100)
- [ ] Response includes pagination metadata (total pages, total elements)

**Effort**: 1-2 days  
**Priority**: P2 - MEDIUM  
**Assignee**: Backend Team  

---

### 🟢 Item #5: Improve Email Validation

**Problem**: Current regex `^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$` accepts malformed addresses (e.g., consecutive dots).

**Impact**: Invalid email addresses may be accepted.

**Fix**:
```java
// Email.java
private static final Pattern EMAIL_PATTERN = Pattern.compile(
    "^[a-zA-Z0-9_!#$%&'*+/=?`{|}^~-]+(?:\.[a-zA-Z0-9_!#$%&'*+/=?`{|}^~-]+)*@" +
    "(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\.)+[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?$",
    Pattern.CASE_INSENSITIVE);
```

**Acceptance Criteria**:
- [ ] Rejects `user..name@example.com` (consecutive dots)
- [ ] Accepts `user.name@example.com` (valid)
- [ ] Accepts `user+tag@example.com` (valid)
- [ ] Rejects `user@.com` (invalid domain)
- [ ] Existing valid emails still work

**Effort**: 1-2 hours  
**Priority**: P2 - MEDIUM  
**Assignee**: Backend Team  

---

## Retracted Items (From Initial Review)

These items were **incorrectly flagged** and have been **VERIFIED as non-issues**:

| Item | Initial Concern | Verification |
|------|----------------|--------------|
| Dual user tables | `t_admin_users` vs `t_users` | v-3 migrates data and drops old table ✅ |
| Duplicate admin creation | Seed + bootstrap | Bootstrap checks for existence ✅ |
| Raw token return | Security concern | Intentional for onboarding ✅ |
| No transaction management | Missing @Transactional | Present on repositories ✅ |
| VendorRepository.create | Missing method | Intentional - onboarding handles it ✅ |

---

## Implementation Roadmap

### Sprint 1 follow-up
- [ ] **P0 #1**: Review and merge PXG-015; execute the agreed test suites
- [ ] **P2 #2**: Confine local JWT secret fallback (1 hour)
- [x] **P1 #3**: Add Bean Validation (implemented in PXG-015; suites not run)

### Remaining review items
- [ ] **P2 #4**: Add pagination to `GET /demo` (vendor listing pagination is complete)
- [ ] **P2 #5**: Improve email validation (1-2 hours)

---

## Test Checklist

Before merging any changes, verify:

- [ ] All existing tests pass
- [ ] New tests added for new functionality
- [ ] Validation tests cover edge cases
- [ ] Security tests verify protection against unauthorized access
- [ ] Performance tests for paginated endpoints

---

## Definition of Done

For each action item:
- [ ] Code changes implemented
- [ ] Tests written and passing
- [ ] Documentation updated (if applicable)
- [ ] Peer review completed
- [ ] Merged to main branch

---

**Document Owner**: Backend Team Lead  
**Last Updated**: 2026-10-31
**Next Review**: After PXG-015 merge and verification
