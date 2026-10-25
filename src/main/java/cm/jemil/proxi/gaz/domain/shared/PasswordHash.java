/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.shared;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;

/** Opaque, already-encoded password credential. */
public record PasswordHash(String value) {

  public PasswordHash {
    if (value == null || value.isBlank()) {
      throw new InvalidDomainValueException("password hash must not be blank");
    }
  }

  public static PasswordHash from(String value) {
    return new PasswordHash(value);
  }
}
