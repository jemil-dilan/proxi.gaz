/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import cm.jemil.proxi.gaz.application.outbound.AuditLogPort;
import cm.jemil.proxi.gaz.application.outbound.Clock;
import cm.jemil.proxi.gaz.application.outbound.UnitOfWork;
import cm.jemil.proxi.gaz.application.outbound.VendorRepository;
import cm.jemil.proxi.gaz.domain.audit.AuditAction;
import cm.jemil.proxi.gaz.domain.audit.AuditEntry;
import cm.jemil.proxi.gaz.domain.audit.AuditMetadata;
import cm.jemil.proxi.gaz.domain.shared.CreatedAt;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SetVendorStatusUseCase {
  private final VendorRepository vendorRepository;
  private final AuditLogPort auditLog;
  private final UnitOfWork unitOfWork;
  private final Clock clock;

  /** Records the request even when the vendor already has that status (decision PXG-061). */
  public void execute(UserId adminUserId, UUID vendorId, boolean isActive) {
    unitOfWork.inTransaction(
        () -> {
          var vendor = vendorRepository.findById(VendorId.from(vendorId));
          var previousActive = vendor.isActive();
          if (isActive) {
            vendor.activate();
          } else {
            vendor.deactivate();
          }
          vendorRepository.update(vendor);
          auditLog.record(
              AuditEntry.vendor(
                  adminUserId,
                  AuditAction.forStatus(isActive),
                  vendorId,
                  AuditMetadata.statusChanged(previousActive),
                  CreatedAt.from(clock.now())));
          return vendor;
        });
  }
}
