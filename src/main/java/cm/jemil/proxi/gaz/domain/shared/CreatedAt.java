/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.shared;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import java.time.Instant;

/** Creation timestamp shared by persisted domain entities. */
public record CreatedAt(Instant value) {

  public CreatedAt {
    if (value == null) {
      throw new InvalidDomainValueException("created-at instant must not be null");
    }
  }

  public static CreatedAt from(Instant value) {
    return new CreatedAt(value);
  }
}
