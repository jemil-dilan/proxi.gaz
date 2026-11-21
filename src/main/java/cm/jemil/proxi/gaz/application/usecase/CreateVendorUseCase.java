/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import cm.jemil.proxi.gaz.application.outbound.AuditLogPort;
import cm.jemil.proxi.gaz.application.outbound.Clock;
import cm.jemil.proxi.gaz.application.outbound.UnitOfWork;
import cm.jemil.proxi.gaz.application.outbound.VendorOnboardingRepository;
import cm.jemil.proxi.gaz.application.outbound.VendorSessionTokenGenerator;
import cm.jemil.proxi.gaz.application.outbound.VendorSessionTokenHasher;
import cm.jemil.proxi.gaz.domain.audit.AuditAction;
import cm.jemil.proxi.gaz.domain.audit.AuditEntry;
import cm.jemil.proxi.gaz.domain.audit.AuditMetadata;
import cm.jemil.proxi.gaz.domain.shared.AddressText;
import cm.jemil.proxi.gaz.domain.shared.CreatedAt;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.shared.GeoPoint;
import cm.jemil.proxi.gaz.domain.shared.Money;
import cm.jemil.proxi.gaz.domain.shared.PhoneNumber;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import cm.jemil.proxi.gaz.domain.user.User;
import cm.jemil.proxi.gaz.domain.vendor.DeliveryFeeType;
import cm.jemil.proxi.gaz.domain.vendor.Vendor;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import cm.jemil.proxi.gaz.domain.vendor.VendorName;
import cm.jemil.proxi.gaz.domain.vendor.VendorSession;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

@RequiredArgsConstructor
public class CreateVendorUseCase {

  private final VendorOnboardingRepository onboardingRepository;
  private final VendorSessionTokenGenerator tokenGenerator;
  private final VendorSessionTokenHasher tokenHasher;
  private final Clock clock;
  private final AuditLogPort auditLog;
  private final UnitOfWork unitOfWork;

  public Response execute(UserId adminUserId, Command command) {
    var createdAt = CreatedAt.from(clock.now());
    var user = User.createVendor(command.email(), createdAt);
    var vendor =
        Vendor.of(
            user.getId(),
            command.name(),
            command.phone(),
            command.location(),
            command.addressText(),
            command.isStation(),
            command.deliveryFeeType(),
            command.deliveryFeeAmount(),
            command.isActive());
    var rawToken = tokenGenerator.generate();
    var session = VendorSession.of(vendor.getId(), tokenHasher.hash(rawToken), createdAt);

    unitOfWork.inTransaction(
        () -> {
          onboardingRepository.insert(user, vendor, session);
          auditLog.record(
              AuditEntry.vendor(
                  adminUserId,
                  AuditAction.VENDOR_CREATED,
                  vendor.id(),
                  AuditMetadata.created(vendor.isActive()),
                  createdAt));
          return vendor.getId();
        });
    return new Response(vendor.getId(), rawToken.value());
  }

  public record Command(
      @Nullable Email email,
      VendorName name,
      PhoneNumber phone,
      GeoPoint location,
      AddressText addressText,
      Boolean isStation,
      DeliveryFeeType deliveryFeeType,
      Money deliveryFeeAmount,
      Boolean isActive) {}

  public record Response(VendorId vendorId, String sessionToken) {}
}
