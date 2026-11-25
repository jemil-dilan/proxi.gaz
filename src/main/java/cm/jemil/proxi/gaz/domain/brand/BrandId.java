/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.brand;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import java.util.UUID;

public record BrandId(UUID value) {
  public BrandId {
    if (value == null) {
      throw new InvalidDomainValueException("brand id must not be null");
    }
  }

  public static BrandId generate() {
    return new BrandId(UUID.randomUUID());
  }

  public static BrandId from(UUID value) {
    return new BrandId(value);
  }
}
