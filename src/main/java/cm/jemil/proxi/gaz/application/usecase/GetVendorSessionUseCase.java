/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import cm.jemil.proxi.gaz.application.outbound.VendorRepository;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import cm.jemil.proxi.gaz.domain.vendor.VendorInactiveException;
import cm.jemil.proxi.gaz.domain.vendor.VendorName;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetVendorSessionUseCase {
  private final VendorRepository vendorRepository;

  public Response execute(UUID vendorId) {
    var vendor = vendorRepository.findById(VendorId.from(vendorId));
    if (!vendor.isActive()) {
      throw new VendorInactiveException();
    }

    return new Response(vendor.getId(), vendor.getName(), vendor.isActive());
  }

  public record Response(VendorId vendorId, VendorName name, Boolean isActive) {}
}
