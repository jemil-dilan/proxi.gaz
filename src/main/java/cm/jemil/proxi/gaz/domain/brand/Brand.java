/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.brand;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import cm.jemil.proxi.gaz.domain.shared.CreatedAt;

/** A gas brand of the reference catalogue (PXG-064). Vendors sell brands from this list only. */
public record Brand(BrandId id, BrandName name, boolean active, CreatedAt createdAt) {

  public Brand {
    if (id == null || name == null || createdAt == null) {
      throw new InvalidDomainValueException("brand fields must not be null");
    }
  }

  /** New catalogue entries are active. */
  public static Brand create(BrandName name, CreatedAt createdAt) {
    return new Brand(BrandId.generate(), name, true, createdAt);
  }
}
