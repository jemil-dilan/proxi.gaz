/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.vendor;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;

public record VendorSessionTokenHash(String value) {
  public VendorSessionTokenHash {
    if (value == null || value.isBlank()) {
      throw new InvalidDomainValueException("vendor session token hash must not be blank");
    }
  }
}
