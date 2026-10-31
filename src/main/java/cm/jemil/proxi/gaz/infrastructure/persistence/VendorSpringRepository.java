/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface VendorSpringRepository extends JpaRepository<VendorJpa, UUID> {

  Optional<VendorJpa> findAggregateById(UUID id);

  Optional<VendorJpa> findAggregateByUserId(UUID userId);

  @Query(
      """
      select vendor from VendorJpa vendor
      where (:active is null or vendor.active = :active)
        and (:name is null or lower(vendor.name) like lower(concat('%', :name, '%')))
      order by lower(vendor.name), vendor.id
      """)
  Page<VendorJpa> search(
      @Param("active") Boolean active, @Param("name") String name, Pageable pageable);
}
