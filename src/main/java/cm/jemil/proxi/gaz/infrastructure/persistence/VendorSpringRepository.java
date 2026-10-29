/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface VendorSpringRepository extends JpaRepository<VendorJpa, UUID> {

  Optional<VendorJpa> findAggregateById(UUID id);

  Optional<VendorJpa> findAggregateByUserId(UUID userId);
}
