/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.vendor;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;

public record VendorName(String value) {
  public VendorName {
    if (value == null || value.isBlank() || value.trim().length() > 255) {
      throw new InvalidDomainValueException(
          "vendor name must be non-blank and at most 255 characters");
    }
    value = value.trim();
  }

  public static VendorName from(String value) {
    return new VendorName(value);
  }
}
