/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.shared;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;

/** A normalized phone number bounded by the persistence contract. */
public record PhoneNumber(String value) {
  public PhoneNumber {
    if (value == null || value.isBlank() || value.trim().length() > 32) {
      throw new InvalidDomainValueException("phone must be non-blank and at most 32 characters");
    }
    value = value.trim();
  }

  public static PhoneNumber from(String value) {
    return new PhoneNumber(value);
  }
}
