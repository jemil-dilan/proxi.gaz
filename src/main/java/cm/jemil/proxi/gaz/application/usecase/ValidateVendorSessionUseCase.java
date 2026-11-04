/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import cm.jemil.proxi.gaz.application.outbound.VendorRepository;
import cm.jemil.proxi.gaz.application.outbound.VendorRepository;
import cm.jemil.proxi.gaz.application.outbound.VendorSessionRepository;
import cm.jemil.proxi.gaz.application.outbound.VendorSessionTokenHasher;
import cm.jemil.proxi.gaz.domain.vendor.InvalidVendorSessionException;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import cm.jemil.proxi.gaz.domain.vendor.VendorInactiveException;
import cm.jemil.proxi.gaz.domain.vendor.VendorSessionToken;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ValidateVendorSessionUseCase {
  private final VendorSessionRepository repository;
  private final VendorSessionTokenHasher tokenHasher;
  private final VendorRepository vendorRepository;

  public Result execute(String rawToken) {
    if (rawToken == null || rawToken.isBlank()) {
      throw new InvalidVendorSessionException();
    }
    var token = new VendorSessionToken(rawToken);
    var session =
        repository
            .findByTokenHash(tokenHasher.hash(token))
            .orElseThrow(InvalidVendorSessionException::new);
    var vendor = vendorRepository.findById(session.vendorId());
    if (!vendor.isActive()) {
      throw new VendorInactiveException(session.vendorId());
    }
    return new Result(session.vendorId(), vendor.getName().value(), vendor.isActive());
  }

  public record Result(VendorId vendorId, String name, boolean isActive) {}
}
