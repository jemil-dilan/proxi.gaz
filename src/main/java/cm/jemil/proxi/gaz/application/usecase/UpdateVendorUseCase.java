/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import cm.jemil.proxi.gaz.application.outbound.VendorRepository;
import cm.jemil.proxi.gaz.domain.vendor.VendorDetails;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import cm.jemil.proxi.gaz.domain.vendor.VendorNotFoundException;
import java.util.UUID;

public class UpdateVendorUseCase {
  private final VendorRepository repository;

  public UpdateVendorUseCase(VendorRepository repository) {
    this.repository = repository;
  }

  public void execute(Command command) {
    var vendor =
        repository
            .findById(VendorId.from(command.vendorId()))
            .orElseThrow(VendorNotFoundException::new);
    vendor.replaceDetails(
        VendorDetails.from(
            command.name(),
            command.phone(),
            command.latitude(),
            command.longitude(),
            command.addressText(),
            command.isStation(),
            command.deliveryFeeType(),
            command.deliveryFeeAmount(),
            command.isActive()));
    repository.update(vendor);
  }

  public record Command(
      UUID vendorId,
      String name,
      String phone,
      Double latitude,
      Double longitude,
      String addressText,
      Boolean isStation,
      String deliveryFeeType,
      Integer deliveryFeeAmount,
      Boolean isActive) {}
}
