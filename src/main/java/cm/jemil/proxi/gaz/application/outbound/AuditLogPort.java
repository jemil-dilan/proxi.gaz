/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.outbound;

import cm.jemil.proxi.gaz.application.views.AuditLogView;
import cm.jemil.proxi.gaz.domain.audit.AuditEntry;
import cm.jemil.proxi.gaz.domain.audit.AuditTargetType;
import cm.jemil.proxi.gaz.domain.shared.PageData;
import cm.jemil.proxi.gaz.domain.shared.PaginationFetchRequest;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** Append-only admin audit trail. There is deliberately no update or delete. */
public interface AuditLogPort {

  /** Stores the entry in the caller's transaction. */
  void record(AuditEntry entry);

  /** Entries newest first, optionally restricted to a target type and id. */
  PageData<AuditLogView> find(
      @Nullable AuditTargetType targetType,
      @Nullable UUID targetId,
      PaginationFetchRequest paginationFetchRequest);
}
