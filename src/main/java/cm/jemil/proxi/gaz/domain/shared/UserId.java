/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.shared;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import java.util.UUID;

/** Identifier shared by user aggregates. */
public record UserId(UUID value) {

  public UserId {
    if (value == null) {
      throw new InvalidDomainValueException("user id must not be null");
    }
  }

  public static UserId generate() {
    return new UserId(UUID.randomUUID());
  }

  public static UserId from(UUID value) {
    return new UserId(value);
  }
}
