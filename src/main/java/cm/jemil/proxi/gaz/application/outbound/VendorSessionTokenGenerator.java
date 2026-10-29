/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.outbound;

import cm.jemil.proxi.gaz.domain.vendor.VendorSessionToken;

/** Supplies high-entropy raw bearer secrets for one-time vendor onboarding. */
public interface VendorSessionTokenGenerator {

  VendorSessionToken generate();
}
