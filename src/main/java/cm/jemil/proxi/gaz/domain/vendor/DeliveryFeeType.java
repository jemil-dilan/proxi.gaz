/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.vendor;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import java.util.Optional;

public record DeliveryFeeType(String value) {
  public DeliveryFeeType {
    if (value == null || value.isBlank() || value.trim().length() > 32) {
      throw new InvalidDomainValueException(
          "delivery fee type must be non-blank and at most 32 characters");
    }
    value = value.trim();
  }

  public static Optional<DeliveryFeeType> fromOptional(String value) {
    return value == null || value.isBlank()
        ? Optional.empty()
        : Optional.of(new DeliveryFeeType(value));
  }

  public static DeliveryFeeType from(String value) {
    return new DeliveryFeeType(value);
  }
}
