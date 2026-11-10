/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.vendor;

import cm.jemil.proxi.gaz.domain.shared.CreatedAt;
import cm.jemil.proxi.gaz.domain.shared.LastUsedAt;
import cm.jemil.proxi.gaz.domain.shared.RevokedAt;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Persisted vendor-session identity; the raw bearer token is deliberately not part of this type.
 */
public record VendorSession(
    VendorSessionId id,
    VendorId vendorId,
    VendorSessionTokenHash tokenHash,
    CreatedAt createdAt,
    @Nullable LastUsedAt lastUsedAt,
    @Nullable RevokedAt revokedAt) {

  public static VendorSession of(
      VendorId vendorId, VendorSessionTokenHash tokenHash, CreatedAt createdAt) {
    return new VendorSession(
        VendorSessionId.generate(), vendorId, tokenHash, createdAt, null, null);
  }

  public VendorSession withLastUsedAt(LastUsedAt lastUsedAt) {
    return new VendorSession(id, vendorId, tokenHash, createdAt, lastUsedAt, revokedAt);
  }

  public VendorSession withRevokedAt(RevokedAt revokedAt) {
    return new VendorSession(id, vendorId, tokenHash, createdAt, lastUsedAt, revokedAt);
  }

  public boolean isActive() {
    return Objects.isNull(revokedAt);
  }
}
