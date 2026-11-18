/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.views;

import cm.jemil.proxi.gaz.domain.audit.AuditEntry;
import org.jspecify.annotations.Nullable;

/** An audit entry with the acting admin's email, for the back office. */
public record AuditLogView(AuditEntry entry, @Nullable String adminEmail) {}
