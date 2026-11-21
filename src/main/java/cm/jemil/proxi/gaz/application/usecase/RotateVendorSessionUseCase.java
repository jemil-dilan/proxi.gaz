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
import cm.jemil.proxi.gaz.application.outbound.VendorSessionTokenGenerator;
import cm.jemil.proxi.gaz.application.outbound.VendorSessionTokenHasher;
import cm.jemil.proxi.gaz.domain.audit.AuditAction;
import cm.jemil.proxi.gaz.domain.audit.AuditEntry;
import cm.jemil.proxi.gaz.domain.audit.AuditMetadata;
import cm.jemil.proxi.gaz.domain.shared.CreatedAt;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import cm.jemil.proxi.gaz.domain.vendor.VendorSession;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RotateVendorSessionUseCase {
  private final VendorRepository vendorRepository;
  private final VendorSessionRepository sessionRepository;
  private final VendorSessionTokenGenerator tokenGenerator;
  private final VendorSessionTokenHasher tokenHasher;
  private final Clock clock;
  private final AuditLogPort auditLog;
  private final UnitOfWork unitOfWork;

  /** Returns the new raw token. The audit entry never contains the token or its hash. */
  public String execute(UserId adminUserId, UUID vendorId) {
    var id = new VendorId(vendorId);
    var token = tokenGenerator.generate();
    var createdAt = CreatedAt.from(clock.now());
    unitOfWork.inTransaction(
        () -> {
          vendorRepository.findById(id);
          var session = VendorSession.of(id, tokenHasher.hash(token), createdAt);
          sessionRepository.replaceForVendor(id, session);
          auditLog.record(
              AuditEntry.vendor(
                  adminUserId,
                  AuditAction.VENDOR_SESSION_ROTATED,
                  vendorId,
                  AuditMetadata.empty(),
                  createdAt));
          return session;
        });
    return token.value();
  }
}
