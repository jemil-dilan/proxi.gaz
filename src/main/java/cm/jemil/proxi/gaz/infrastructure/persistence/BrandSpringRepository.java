/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface BrandSpringRepository extends JpaRepository<BrandJpa, UUID> {

  /** {@code key} is the lower-cased normalised name, matching the unique index on lower(c_name). */
  @Query("SELECT COUNT(b) > 0 FROM BrandJpa b WHERE LOWER(b.name) = :key")
  boolean existsByNameKey(@Param("key") String key);

  @Query("SELECT b FROM BrandJpa b ORDER BY LOWER(b.name), b.name")
  List<BrandJpa> findAllSorted();

  @Query("SELECT b FROM BrandJpa b WHERE b.active = :active ORDER BY LOWER(b.name), b.name")
  List<BrandJpa> findAllSorted(@Param("active") boolean active);
}
