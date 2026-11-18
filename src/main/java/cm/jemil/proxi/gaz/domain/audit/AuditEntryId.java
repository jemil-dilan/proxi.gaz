/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.audit;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import java.util.UUID;

public record AuditEntryId(UUID value) {
  public AuditEntryId {
    if (value == null) {
      throw new InvalidDomainValueException("audit entry id must not be null");
    }
  }

  public static AuditEntryId generate() {
    return new AuditEntryId(UUID.randomUUID());
  }

  public static AuditEntryId from(UUID value) {
    return new AuditEntryId(value);
  }
}
