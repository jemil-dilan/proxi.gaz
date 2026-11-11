/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.shared;

import java.time.Instant;
import java.util.Optional;

/** Last used timestamp for tracking when a session was last accessed. */
public record LastUsedAt(Instant value) {

  public static LastUsedAt from(Instant value) {
    return new LastUsedAt(value);
  }

  public static Optional<LastUsedAt> fromOptional(Instant value) {
    return Optional.ofNullable(value).map(LastUsedAt::from);
  }
}
