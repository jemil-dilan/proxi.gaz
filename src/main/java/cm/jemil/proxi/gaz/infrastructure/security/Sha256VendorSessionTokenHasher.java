/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.security;

import cm.jemil.proxi.gaz.application.outbound.VendorSessionTokenHasher;
import cm.jemil.proxi.gaz.domain.vendor.VendorSessionToken;
import cm.jemil.proxi.gaz.domain.vendor.VendorSessionTokenHash;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class Sha256VendorSessionTokenHasher implements VendorSessionTokenHasher {

  @Override
  public VendorSessionTokenHash hash(VendorSessionToken token) {
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256")
              .digest(token.value().getBytes(StandardCharsets.UTF_8));
      return new VendorSessionTokenHash(HexFormat.of().formatHex(digest));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is not available", exception);
    }
  }
}
