/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.shared;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import java.util.Locale;
import java.util.regex.Pattern;

/** Normalized email address shared by domain users. */
public record Email(String value) {

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

  public Email {
    if (value == null) {
      throw new InvalidDomainValueException("email must not be null");
    }
    value = value.trim().toLowerCase(Locale.ROOT);
    if (!EMAIL_PATTERN.matcher(value).matches()) {
      throw new InvalidDomainValueException("email is invalid");
    }
  }

  public static Email from(String value) {
    return new Email(value);
  }
}
