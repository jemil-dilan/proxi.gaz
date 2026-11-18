/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.audit;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import cm.jemil.proxi.gaz.domain.shared.CreatedAt;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import java.util.UUID;

/** One admin write action. Append-only: entries are never updated or deleted. */
public record AuditEntry(
    AuditEntryId id,
    UserId adminUserId,
    AuditAction action,
    AuditTargetType targetType,
    UUID targetId,
    AuditMetadata metadata,
    CreatedAt createdAt) {

  public AuditEntry {
    if (id == null
        || adminUserId == null
        || action == null
        || targetType == null
        || targetId == null
        || metadata == null
        || createdAt == null) {
      throw new InvalidDomainValueException("audit entry fields must not be null");
    }
  }

  public static AuditEntry vendor(
      UserId adminUserId,
      AuditAction action,
      UUID vendorId,
      AuditMetadata metadata,
      CreatedAt createdAt) {
    return new AuditEntry(
        AuditEntryId.generate(),
        adminUserId,
        action,
        AuditTargetType.VENDOR,
        vendorId,
        metadata,
        createdAt);
  }
}
