/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.brand;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * A brand's display name: trimmed, inner whitespace collapsed to single spaces, 1–64 characters.
 * Names are unique by {@link #key()}, so "Green Gas" and " green GAS" are the same brand.
 */
public record BrandName(String value) {

  public static final int MAX_LENGTH = 64;
  private static final Pattern WHITESPACE = Pattern.compile("\\s+");

  public BrandName {
    if (value == null) {
      throw new InvalidDomainValueException("brand name must not be blank");
    }
    value = WHITESPACE.matcher(value.strip()).replaceAll(" ");
    if (value.isEmpty()) {
      throw new InvalidDomainValueException("brand name must not be blank");
    }
    if (value.length() > MAX_LENGTH) {
      throw new InvalidDomainValueException(
          "brand name must be at most " + MAX_LENGTH + " characters");
    }
  }

  public static BrandName from(String value) {
    return new BrandName(value);
  }

  /** Comparison key: the normalised name in lower case (matches the database unique index). */
  public String key() {
    return value.toLowerCase(Locale.ROOT);
  }
}
