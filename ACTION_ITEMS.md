# Action Items - ProxiGaz Backend Code Review

**Source**: [`CODE_REVIEW.md`](CODE_REVIEW.md), current review (2026-10-29)  
**Status**: Review backlog; implementation deferred until PXG-012B review/merge is agreed

---

## Priority Legend

- **P0 - BLOCKER**: Must fix before vendors can use the system
- **P1 - HIGH**: Should fix before next deployment
- **P2 - MEDIUM**: Nice to have, implement when capacity allows
- **P3 - LOW**: Future improvements

---

## P0 - BLOCKER (Vendor Authentication Required)

### 🔴 Item #1: Complete Vendor Session Authentication

**Problem**: Vendor onboarding creates a session token but there's no way for vendors to use it.

**Impact**: Vendors cannot authenticate or access vendor-specific endpoints.

**Files to Create/Modify**:
- `specs/openapi/main.yaml` - Add vendor session endpoints
- `src/main/java/cm/jemil/proxi/gaz/application/outbound/VendorSessionRepository.java` - Add findByTokenHash method
- `src/main/java/cm/jemil/proxi/gaz/application/usecase/ValidateVendorSessionUseCase.java` - New use case
- `src/main/java/cm/jemil/proxi/gaz/interfaces/rest/VendorSessionController.java` - New controller
- `src/main/java/cm/jemil/proxi/gaz/interfaces/rest/RestMapper.java` - Add mapping methods

**Acceptance Criteria**:
- [ ] POST `/vendor/sessions/validate` endpoint exists
- [ ] Validates vendor session token against stored hash
- [ ] Returns vendor ID and scopes on success
- [ ] Returns 401 for invalid/expired tokens
- [ ] Vendor-specific endpoints secured with session validation

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

**Problem**: Generated API interfaces already use `@Valid` and DTOs have `@NotNull` for required fields, but the build declares only `jakarta.validation-api` and no runtime validation provider. The OpenAPI schemas also lack useful length/range constraints, so API-boundary enforcement is incomplete.

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

**Problem**: `GET /demo` and other list endpoints return all records without pagination.

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

### Week 1 (Sprint Completion)
- [ ] **P0 #1**: Complete vendor session authentication (2-3 days)
- [ ] **P2 #2**: Confine local JWT secret fallback (1 hour)
- [ ] **P1 #3**: Add Bean Validation (2-3 days)

### Week 2 (Next Sprint)
- [ ] **P2 #4**: Add pagination (1-2 days)
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
**Last Updated**: 2026-10-29  
**Next Review**: After P0 items completed
