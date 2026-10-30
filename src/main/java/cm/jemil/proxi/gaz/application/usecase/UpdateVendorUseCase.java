/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import cm.jemil.proxi.gaz.application.outbound.VendorRepository;
import cm.jemil.proxi.gaz.domain.shared.AddressText;
import cm.jemil.proxi.gaz.domain.shared.GeoPoint;
import cm.jemil.proxi.gaz.domain.shared.Money;
import cm.jemil.proxi.gaz.domain.shared.PhoneNumber;
import cm.jemil.proxi.gaz.domain.vendor.DeliveryFeeType;
import cm.jemil.proxi.gaz.domain.vendor.Vendor;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import cm.jemil.proxi.gaz.domain.vendor.VendorName;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateVendorUseCase {
  private final VendorRepository repository;

  public void execute(Command command) {
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
