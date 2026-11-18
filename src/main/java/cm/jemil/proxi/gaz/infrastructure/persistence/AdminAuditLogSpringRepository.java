/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface AdminAuditLogSpringRepository
    extends JpaRepository<AdminAuditLogJpa, UUID>, JpaSpecificationExecutor<AdminAuditLogJpa> {}
