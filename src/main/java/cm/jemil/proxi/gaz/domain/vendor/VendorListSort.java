/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.vendor;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import org.jspecify.annotations.Nullable;

/** Sort field and direction for admin vendor listing. */
public record VendorListSort(Field field, Direction direction) {

  public static final VendorListSort DEFAULT = new VendorListSort(Field.NAME, Direction.ASC);

  public enum Field {
    NAME,
    CREATED_AT
  }

  public enum Direction {
    ASC,
    DESC
  }

  public static VendorListSort parse(@Nullable String sort) {
    if (sort == null || sort.isBlank()) {
      return DEFAULT;
    }
    return switch (sort) {
      case "name,asc" -> new VendorListSort(Field.NAME, Direction.ASC);
      case "name,desc" -> new VendorListSort(Field.NAME, Direction.DESC);
      case "createdAt,asc" -> new VendorListSort(Field.CREATED_AT, Direction.ASC);
      case "createdAt,desc" -> new VendorListSort(Field.CREATED_AT, Direction.DESC);
      default -> throw new InvalidDomainValueException("unsupported vendor sort: " + sort);
    };
  }
}
