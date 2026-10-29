/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.outbound;

import cm.jemil.proxi.gaz.domain.vendor.VendorSessionToken;
import cm.jemil.proxi.gaz.domain.vendor.VendorSessionTokenHash;

/** Hashes high-entropy opaque session tokens before persistence. */
public interface VendorSessionTokenHasher {

  VendorSessionTokenHash hash(VendorSessionToken token);
}
