/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.outbound;

import cm.jemil.proxi.gaz.domain.user.User;
import cm.jemil.proxi.gaz.domain.vendor.Vendor;
import cm.jemil.proxi.gaz.domain.vendor.VendorSession;

/** Atomic persistence boundary for onboarding a User, Vendor, and initial session. */
public interface VendorOnboardingRepository {

  void insert(User user, Vendor vendor, VendorSession vendorSession);
}
