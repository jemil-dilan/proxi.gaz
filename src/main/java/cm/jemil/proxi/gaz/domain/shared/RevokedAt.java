/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.shared;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Value object representing when a session was revoked. Null/empty means the session is still
 * active.
 */
public record RevokedAt(Instant value) {

  public static RevokedAt from(Instant instant) {
    return new RevokedAt(Objects.requireNonNull(instant, "RevokedAt instant cannot be null"));
  }

  public static Optional<RevokedAt> fromOptional(Instant instant) {
    return Optional.ofNullable(instant).map(RevokedAt::from);
  }

  public boolean isAfter(RevokedAt other) {
    return this.value.isAfter(other.value);
  }

  public boolean isBefore(RevokedAt other) {
    return this.value.isBefore(other.value);
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
