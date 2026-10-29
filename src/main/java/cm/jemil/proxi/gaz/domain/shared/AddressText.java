/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.shared;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import java.util.Optional;

/** Optional human-readable address shared by location-bearing domain concepts. */
public record AddressText(String value) {
  public AddressText {
    if (value == null || value.trim().isEmpty() || value.trim().length() > 512) {
      throw new InvalidDomainValueException(
          "address text must be non-blank and at most 512 characters");
    }
    value = value.trim();
  }

  public static Optional<AddressText> fromOptional(String value) {
    return value == null || value.isBlank()
        ? Optional.empty()
        : Optional.of(new AddressText(value));
  }

  public static AddressText from(String value) {
    return new AddressText(value);
  }
}
