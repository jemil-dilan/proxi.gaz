/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import cm.jemil.proxi.gaz.application.outbound.AuditLogPort;
import cm.jemil.proxi.gaz.application.outbound.Clock;
import cm.jemil.proxi.gaz.application.outbound.UnitOfWork;
import cm.jemil.proxi.gaz.application.outbound.VendorRepository;
import cm.jemil.proxi.gaz.application.outbound.VendorSessionRepository;
import cm.jemil.proxi.gaz.domain.audit.AuditAction;
import cm.jemil.proxi.gaz.domain.audit.AuditEntry;
import cm.jemil.proxi.gaz.domain.audit.AuditMetadata;
import cm.jemil.proxi.gaz.domain.shared.CreatedAt;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RevokeVendorSessionUseCase {
  private final VendorRepository vendorRepository;
  private final VendorSessionRepository sessionRepository;
  private final AuditLogPort auditLog;
  private final UnitOfWork unitOfWork;
  private final Clock clock;

  public void execute(UserId adminUserId, UUID vendorId) {
    var id = new VendorId(vendorId);
    unitOfWork.inTransaction(
        () -> {
          vendorRepository.findById(id);
          sessionRepository.revokeForVendor(id);
          auditLog.record(
              AuditEntry.vendor(
                  adminUserId,
                  AuditAction.VENDOR_SESSION_REVOKED,
                  vendorId,
                  AuditMetadata.empty(),
                  CreatedAt.from(clock.now())));
          return id;
        });
  }
}
