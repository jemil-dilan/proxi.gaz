/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.audit;

/** Admin write actions recorded in the audit log. Stored by name: never rename a constant. */
public enum AuditAction {
  VENDOR_CREATED,
  VENDOR_UPDATED,
  VENDOR_ACTIVATED,
  VENDOR_DEACTIVATED,
  VENDOR_SESSION_ROTATED,
  VENDOR_SESSION_REVOKED,
  BRAND_CREATED;

  /** Action recorded by a status change, named after the requested state. */
  public static AuditAction forStatus(boolean isActive) {
    return isActive ? VENDOR_ACTIVATED : VENDOR_DEACTIVATED;
  }
}
