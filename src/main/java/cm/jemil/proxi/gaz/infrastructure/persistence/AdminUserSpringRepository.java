/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface AdminUserSpringRepository extends JpaRepository<AdminUserJpa, UUID> {

  Optional<AdminUserJpa> findByEmail(String email);

  boolean existsByEmail(String email);
}
