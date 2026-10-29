/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.vendor;

import cm.jemil.proxi.gaz.domain.shared.CreatedAt;

/**
 * Persisted vendor-session identity; the raw bearer token is deliberately not part of this type.
 */
public record VendorSession(
    VendorSessionId id, VendorId vendorId, VendorSessionTokenHash tokenHash, CreatedAt createdAt) {

  public static VendorSession of(
      VendorId vendorId, VendorSessionTokenHash tokenHash, CreatedAt createdAt) {
    return new VendorSession(VendorSessionId.generate(), vendorId, tokenHash, createdAt);
  }
}
