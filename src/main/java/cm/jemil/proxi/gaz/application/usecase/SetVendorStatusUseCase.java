/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import cm.jemil.proxi.gaz.application.outbound.VendorRepository;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SetVendorStatusUseCase {
  private final VendorRepository vendorRepository;

  public void execute(UUID vendorId, boolean isActive) {
    var vendor = vendorRepository.findById(VendorId.from(vendorId));
    if (isActive) {
      vendor.activate();
    } else {
      vendor.deactivate();
    }
    vendorRepository.update(vendor);
  }
}
