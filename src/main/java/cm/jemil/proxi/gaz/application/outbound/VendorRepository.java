/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.outbound;

import cm.jemil.proxi.gaz.domain.vendor.Vendor;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import java.util.Optional;

public interface VendorRepository {
  void insert(Vendor vendor);

  Optional<Vendor> findById(VendorId vendorId);

  void update(Vendor vendor);
}
