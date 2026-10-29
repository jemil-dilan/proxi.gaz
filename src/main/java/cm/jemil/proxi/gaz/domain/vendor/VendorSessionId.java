/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.vendor;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import java.util.UUID;

public record VendorSessionId(UUID value) {
  public VendorSessionId {
    if (value == null) {
      throw new InvalidDomainValueException("vendor session id must not be null");
    }
  }

  public static VendorSessionId generate() {
    return new VendorSessionId(UUID.randomUUID());
  }

  public static VendorSessionId from(UUID value) {
    return new VendorSessionId(value);
  }
}
