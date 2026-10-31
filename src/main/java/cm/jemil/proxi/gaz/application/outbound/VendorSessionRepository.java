/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.outbound;

import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import cm.jemil.proxi.gaz.domain.vendor.VendorSession;
import cm.jemil.proxi.gaz.domain.vendor.VendorSessionTokenHash;
import java.util.Optional;

/** Persistence port for active, hashed vendor sessions. */
public interface VendorSessionRepository {

  Optional<VendorSession> findByTokenHash(VendorSessionTokenHash tokenHash);

  /** Atomically replaces any active session for this vendor with the supplied session. */
  void replaceForVendor(VendorId vendorId, VendorSession session);

  /** Revokes every active session for this vendor. */
  void revokeForVendor(VendorId vendorId);
}
