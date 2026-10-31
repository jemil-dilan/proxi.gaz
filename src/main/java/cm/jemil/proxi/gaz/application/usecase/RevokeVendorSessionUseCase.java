/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import cm.jemil.proxi.gaz.application.outbound.VendorRepository;
import cm.jemil.proxi.gaz.application.outbound.VendorSessionRepository;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RevokeVendorSessionUseCase {
  private final VendorRepository vendorRepository;
  private final VendorSessionRepository sessionRepository;

  public void execute(UUID vendorId) {
    var id = new VendorId(vendorId);
    vendorRepository.findById(id);
    sessionRepository.revokeForVendor(id);
  }
}
