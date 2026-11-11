/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import cm.jemil.proxi.gaz.application.outbound.VendorRepository;
import cm.jemil.proxi.gaz.application.views.VendorView.VendorView1;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetVendorByIdUseCase {
  private final VendorRepository vendorRepository;

  public VendorView1 execute(UUID vendorId) {
    return vendorRepository.findViewById(VendorId.from(vendorId));
  }
}
