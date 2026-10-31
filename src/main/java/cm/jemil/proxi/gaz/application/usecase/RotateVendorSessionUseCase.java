/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import cm.jemil.proxi.gaz.application.outbound.Clock;
import cm.jemil.proxi.gaz.application.outbound.VendorRepository;
import cm.jemil.proxi.gaz.application.outbound.VendorSessionRepository;
import cm.jemil.proxi.gaz.application.outbound.VendorSessionTokenGenerator;
import cm.jemil.proxi.gaz.application.outbound.VendorSessionTokenHasher;
import cm.jemil.proxi.gaz.domain.shared.CreatedAt;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import cm.jemil.proxi.gaz.domain.vendor.VendorSession;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RotateVendorSessionUseCase {
  private final VendorRepository vendorRepository;
  private final VendorSessionRepository sessionRepository;
  private final VendorSessionTokenGenerator tokenGenerator;
  private final VendorSessionTokenHasher tokenHasher;
  private final Clock clock;

  public String execute(UUID vendorId) {
    var id = new VendorId(vendorId);
    vendorRepository.findById(id);
    var token = tokenGenerator.generate();
    var session = VendorSession.of(id, tokenHasher.hash(token), CreatedAt.from(clock.now()));
    sessionRepository.replaceForVendor(id, session);
    return token.value();
  }
}
