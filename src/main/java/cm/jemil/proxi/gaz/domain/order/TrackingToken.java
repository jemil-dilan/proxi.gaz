/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.order;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import java.util.UUID;

/** Opaque public tracking token (never the sequential order id). */
public record TrackingToken(String value) {

  public TrackingToken {
    if (value == null || value.isBlank()) {
      throw new InvalidDomainValueException("tracking token must not be blank");
    }
    value = value.trim();
  }

  public static TrackingToken generate() {
    return new TrackingToken(UUID.randomUUID().toString());
  }

  public static TrackingToken from(String value) {
    return new TrackingToken(value);
  }
}
