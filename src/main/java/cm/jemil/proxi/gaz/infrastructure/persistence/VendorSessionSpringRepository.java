/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface VendorSessionSpringRepository extends JpaRepository<VendorSessionJpa, UUID> {

  Optional<VendorSessionJpa> findByTokenHash(String tokenHash);

  void deleteByVendorId(UUID vendorId);
}
