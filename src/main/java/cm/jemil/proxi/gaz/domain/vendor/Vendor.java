/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.vendor;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;

import java.util.UUID;

public class Vendor {
  private final VendorId id;
  private VendorDetails details;

  private Vendor(VendorId id, VendorDetails details) {
    if (id == null || details == null) {
      throw new InvalidDomainValueException("vendor identity and details are required");
    }
    this.id = id;
    this.details = details;
  }

  public static Vendor create(VendorDetails details) {
    return new Vendor(VendorId.generate(), details);
  }

  public static Vendor reconstitute(VendorId id, VendorDetails details) {
    return new Vendor(id, details);
  }

  public UUID id() {
    return id.value();
  }

  public VendorDetails details() {
    return details;
  }

  public void replaceDetails(VendorDetails details) {
    if (details == null) {
      throw new InvalidDomainValueException("vendor details are required");
    }
    this.details = details;
  }
}
