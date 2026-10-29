/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import cm.jemil.proxi.gaz.application.outbound.VendorSessionRepository;
import cm.jemil.proxi.gaz.application.outbound.VendorSessionTokenHasher;
import cm.jemil.proxi.gaz.domain.vendor.InvalidVendorSessionException;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import cm.jemil.proxi.gaz.domain.vendor.VendorSessionToken;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ValidateVendorSessionUseCase {
  private final VendorSessionRepository repository;
  private final VendorSessionTokenHasher tokenHasher;

  public Result execute(String rawToken) {
    if (rawToken == null || rawToken.isBlank()) {
      throw new InvalidVendorSessionException();
    }
    var token = new VendorSessionToken(rawToken);
    var session =
        repository
            .findByTokenHash(tokenHasher.hash(token))
            .orElseThrow(InvalidVendorSessionException::new);
    return new Result(session.vendorId());
  }

  public record Result(VendorId vendorId) {}
}
