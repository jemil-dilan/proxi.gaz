/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import cm.jemil.proxi.gaz.application.outbound.VendorRepository;
import cm.jemil.proxi.gaz.domain.vendor.Vendor;
import cm.jemil.proxi.gaz.domain.vendor.VendorDetails;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;

import java.util.UUID;

public class CreateVendorUseCase {
  private final VendorRepository repository;

  public CreateVendorUseCase(VendorRepository repository) {
    this.repository = repository;
  }

  public UUID execute(Command command) {
    var vendor = Vendor.create(command.toDetails());
    repository.insert(vendor);
    return vendor.id();
  }

  public record Command(
      String name,
      String phone,
      Double latitude,
      Double longitude,
      String addressText,
      Boolean isStation,
      String deliveryFeeType,
      Integer deliveryFeeAmount,
      Boolean isActive) {
    VendorDetails toDetails() {
      return VendorDetails.from(
          name,
          phone,
          latitude,
          longitude,
          addressText,
          isStation,
          deliveryFeeType,
          deliveryFeeAmount,
          isActive);
    }
  }
}
