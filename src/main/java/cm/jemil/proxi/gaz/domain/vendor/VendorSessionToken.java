/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.vendor;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;

/** Raw bearer secret. Return it once at issuance; never persist or log it. */
public record VendorSessionToken(String value) {
  public VendorSessionToken {
    if (value == null || value.isBlank()) {
      throw new InvalidDomainValueException("vendor session token must not be blank");
    }
  }
}
