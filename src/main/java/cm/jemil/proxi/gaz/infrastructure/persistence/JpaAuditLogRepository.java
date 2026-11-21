/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import cm.jemil.proxi.gaz.application.outbound.AuditLogPort;
import cm.jemil.proxi.gaz.application.views.AuditLogView;
import cm.jemil.proxi.gaz.domain.audit.AuditEntry;
import cm.jemil.proxi.gaz.domain.audit.AuditTargetType;
import cm.jemil.proxi.gaz.domain.shared.PageData;
import cm.jemil.proxi.gaz.domain.shared.PaginationFetchRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
class JpaAuditLogRepository implements AuditLogPort {

  private static final Sort NEWEST_FIRST =
      Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("seq"));

  private final AdminAuditLogSpringRepository auditSpringRepository;
  private final UserSpringRepository userSpringRepository;
  private final JpaMapper jpaMapper;

  /** MANDATORY: an audit row is only ever written as part of the business transaction. */
  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public void record(AuditEntry entry) {
    auditSpringRepository.save(jpaMapper.toJpa(entry));
  }

  @Override
  @Transactional(readOnly = true)
  public PageData<AuditLogView> find(
      @Nullable AuditTargetType targetType,
      @Nullable UUID targetId,
      PaginationFetchRequest paginationFetchRequest) {
    var pageable =
        PageRequest.of(paginationFetchRequest.page(), paginationFetchRequest.limit(), NEWEST_FIRST);
    var page = auditSpringRepository.findAll(matching(targetType, targetId), pageable);
    var emails = adminEmails(page.map(AdminAuditLogJpa::getAdminUserId).toSet());
    var views =
        page.map(row -> new AuditLogView(jpaMapper.toDomain(row), emails.get(row.getAdminUserId())))
            .getContent();
    return new PageData<>(
        views, page.getTotalElements(), page.getTotalPages(), page.getSize(), page.getNumber());
  }

  /** Only the filters that are present become predicates, so no null is ever bound. */
  private static Specification<AdminAuditLogJpa> matching(
      @Nullable AuditTargetType targetType, @Nullable UUID targetId) {
    Specification<AdminAuditLogJpa> spec = Specification.unrestricted();
    if (targetType != null) {
      spec = spec.and((root, query, cb) -> cb.equal(root.get("targetType"), targetType.name()));
    }
    if (targetId != null) {
      spec = spec.and((root, query, cb) -> cb.equal(root.get("targetId"), targetId));
    }
    return spec;
  }

  private Map<UUID, String> adminEmails(java.util.Set<UUID> adminIds) {
    if (adminIds.isEmpty()) {
      return Map.of();
    }
    var emails = new HashMap<UUID, String>();
    userSpringRepository.findAllById(adminIds).stream()
        .filter(user -> user.getEmail() != null)
        .forEach(user -> emails.put(user.getId(), user.getEmail()));
    return emails;
  }
}
