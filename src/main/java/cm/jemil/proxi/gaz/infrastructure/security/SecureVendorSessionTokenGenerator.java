/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.security;

import cm.jemil.proxi.gaz.application.outbound.VendorSessionTokenGenerator;
import cm.jemil.proxi.gaz.domain.vendor.VendorSessionToken;
import java.security.SecureRandom;
import java.util.Base64;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class SecureVendorSessionTokenGenerator implements VendorSessionTokenGenerator {

  private final SecureRandom secureRandom;

  @Override
  public VendorSessionToken generate() {
    byte[] secret = new byte[32];
    secureRandom.nextBytes(secret);
    return new VendorSessionToken(Base64.getUrlEncoder().withoutPadding().encodeToString(secret));
  }
}
