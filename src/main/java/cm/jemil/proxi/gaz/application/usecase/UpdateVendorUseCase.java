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
import cm.jemil.proxi.gaz.domain.shared.AddressText;
import cm.jemil.proxi.gaz.domain.shared.CreatedAt;
import cm.jemil.proxi.gaz.domain.shared.GeoPoint;
import cm.jemil.proxi.gaz.domain.shared.Money;
import cm.jemil.proxi.gaz.domain.shared.PhoneNumber;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import cm.jemil.proxi.gaz.domain.vendor.DeliveryFeeType;
import cm.jemil.proxi.gaz.domain.vendor.Vendor;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import cm.jemil.proxi.gaz.domain.vendor.VendorName;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateVendorUseCase {
  private final VendorRepository repository;
  private final AuditLogPort auditLog;
  private final UnitOfWork unitOfWork;
  private final Clock clock;

  public void execute(UserId adminUserId, Command command) {
    unitOfWork.inTransaction(
        () -> {
          var vendor = repository.findById(command.vendorId());
          var updatedVendor =
              Vendor.reconstitute(
                  vendor.getId(),
                  vendor.getUserId(),
                  command.name(),
                  command.phone(),
                  command.geoPoint(),
                  command.addressText(),
                  command.isStation(),
                  command.deliveryFeeType(),
                  command.deliveryFeeAmount(),
                  vendor.isActive());
          repository.update(updatedVendor);
          auditLog.record(
              AuditEntry.vendor(
                  adminUserId,
                  AuditAction.VENDOR_UPDATED,
                  vendor.id(),
                  AuditMetadata.updated(vendor.profileChangesTo(updatedVendor)),
                  CreatedAt.from(clock.now())));
          return updatedVendor;
        });
  }

  public record Command(
      VendorId vendorId,
      VendorName name,
      PhoneNumber phone,
      GeoPoint geoPoint,
      AddressText addressText,
      Boolean isStation,
      DeliveryFeeType deliveryFeeType,
      Money deliveryFeeAmount) {}
}
