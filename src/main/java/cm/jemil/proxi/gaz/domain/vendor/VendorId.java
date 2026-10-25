/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.vendor;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import java.util.UUID;

public record VendorId(UUID value) {
  public VendorId {
    if (value == null) throw new InvalidDomainValueException("vendor id is required");
  }

  public static VendorId generate() {
    return new VendorId(UUID.randomUUID());
  }

  public static VendorId from(UUID value) {
    return new VendorId(value);
  }
}
