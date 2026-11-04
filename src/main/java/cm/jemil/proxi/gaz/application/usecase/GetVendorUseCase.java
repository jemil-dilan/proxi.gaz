/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import cm.jemil.proxi.gaz.application.outbound.VendorRepository;
import cm.jemil.proxi.gaz.application.views.VendorView.VendorDetailView;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import cm.jemil.proxi.gaz.domain.vendor.VendorInactiveException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetVendorUseCase {
  private final VendorRepository vendorRepository;

  public VendorDetailView execute(VendorId vendorId) {
    var detailView = vendorRepository.findDetailById(vendorId);
    if (!detailView.getIsActive()) {
      throw new VendorInactiveException(vendorId);
    }
    return detailView;
  }
}
