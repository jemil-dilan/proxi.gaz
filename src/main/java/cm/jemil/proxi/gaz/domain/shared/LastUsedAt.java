/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.shared;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import java.time.Instant;
import java.util.Optional;

/** Last used timestamp for tracking when a session was last accessed. */
public record LastUsedAt(Instant value) {

  public LastUsedAt {
    if (value == null) {
      throw new InvalidDomainValueException("last-used-at instant must not be null");
    }
  }

  public static LastUsedAt from(Instant value) {
    return new LastUsedAt(value);
  }

  public static Optional<LastUsedAt> fromOptional(Instant value) {
    return Optional.ofNullable(value).map(LastUsedAt::from);
  }
}
