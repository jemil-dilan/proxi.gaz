/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.order;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import java.util.Objects;

/** Vendor delivery confirmation code: exactly 4 digits. */
public record ConfirmationCode(String value) {

  private static final String PATTERN = "\\d{4}";

  public ConfirmationCode {
    Objects.requireNonNull(value, "confirmation code");
    if (!value.matches(PATTERN)) {
      throw new InvalidDomainValueException("confirmation code must be exactly 4 digits");
    }
  }

  public static ConfirmationCode from(String value) {
    return new ConfirmationCode(value);
  }

  public boolean matches(String candidate) {
    return value.equals(candidate);
  }
}
