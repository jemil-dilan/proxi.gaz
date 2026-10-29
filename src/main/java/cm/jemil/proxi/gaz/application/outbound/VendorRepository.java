/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.outbound;

import cm.jemil.proxi.gaz.domain.shared.UserId;
import cm.jemil.proxi.gaz.domain.vendor.Vendor;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import java.util.Optional;

/** Persistence port for Vendor aggregate roots. */
public interface VendorRepository {

  Vendor findById(VendorId id);

  Optional<Vendor> findByUserId(UserId userId);

  void update(Vendor vendor);
}
