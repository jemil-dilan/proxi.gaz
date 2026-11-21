/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import cm.jemil.proxi.gaz.application.outbound.AuditLogPort;
import cm.jemil.proxi.gaz.application.views.AuditLogView;
import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import cm.jemil.proxi.gaz.domain.audit.AuditTargetType;
import cm.jemil.proxi.gaz.domain.shared.PageData;
import cm.jemil.proxi.gaz.domain.shared.PaginationFetchRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

@RequiredArgsConstructor
public class GetAuditLogUseCase {
  private final AuditLogPort auditLog;

  public PageData<AuditLogView> execute(Query query) {
    return auditLog.find(
        query.targetType(),
        query.targetId(),
        new PaginationFetchRequest(query.size(), query.page()));
  }

  public record Query(
      @Nullable AuditTargetType targetType, @Nullable UUID targetId, int page, int size) {
    public Query {
      if (targetId != null && targetType == null) {
        throw new InvalidDomainValueException("targetId requires targetType");
      }
    }
  }
}
