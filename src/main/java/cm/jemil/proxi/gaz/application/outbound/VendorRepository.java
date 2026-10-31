/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.outbound;

import cm.jemil.proxi.gaz.application.views.VendorView.VendorView1;
import cm.jemil.proxi.gaz.domain.shared.PageData;
import cm.jemil.proxi.gaz.domain.shared.PaginationFetchRequest;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import cm.jemil.proxi.gaz.domain.vendor.Vendor;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import cm.jemil.proxi.gaz.domain.vendor.VendorName;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/** Persistence port for Vendor aggregate roots. */
public interface VendorRepository {

  Vendor findById(VendorId id);

  Optional<Vendor> findByUserId(UserId userId);

  PageData<VendorView1> loadAllVendors(
      PaginationFetchRequest paginationFetchRequest, boolean isActive, @Nullable VendorName name);

  void update(Vendor vendor);
}
