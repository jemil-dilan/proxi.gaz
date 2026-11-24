/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.outbound;

import cm.jemil.proxi.gaz.domain.brand.Brand;
import cm.jemil.proxi.gaz.domain.brand.BrandName;
import java.util.List;
import org.jspecify.annotations.Nullable;

/** Persistence port for the brand reference catalogue. */
public interface BrandRepository {

  /**
   * Stores a new brand in the caller's transaction.
   *
   * @throws cm.jemil.proxi.gaz.domain.brand.BrandAlreadyExistsException when the database already
   *     holds the same name ignoring case and spacing (e.g. a concurrent create)
   */
  void insert(Brand brand);

  /** Whether a brand with the same {@link BrandName#key()} exists. */
  boolean existsByName(BrandName name);

  /** Brands sorted by name ignoring case; all of them when {@code active} is null. */
  List<Brand> findAll(@Nullable Boolean active);
}
