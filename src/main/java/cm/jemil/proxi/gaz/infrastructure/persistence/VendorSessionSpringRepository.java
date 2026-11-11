/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface VendorSessionSpringRepository extends JpaRepository<VendorSessionJpa, UUID> {

  @Query(
      "SELECT vs FROM VendorSessionJpa vs WHERE vs.tokenHash = :tokenHash AND vs.revokedAt IS NULL")
  Optional<VendorSessionJpa> findByTokenHash(@Param("tokenHash") String tokenHash);

  @Query(
      """
      SELECT vs FROM VendorSessionJpa vs
      WHERE vs.vendorId = :vendorId AND vs.revokedAt IS NULL
      ORDER BY vs.createdAt DESC
      LIMIT 1
      """)
  Optional<VendorSessionJpa> findFirstActiveByVendorIdOrderByCreatedAtDesc(
      @Param("vendorId") UUID vendorId);

  @Modifying
  @Query(
      """
      UPDATE VendorSessionJpa vs
      SET vs.revokedAt = CURRENT_TIMESTAMP
      WHERE vs.vendorId = :vendorId AND vs.revokedAt IS NULL
      """)
  void softDeleteByVendorId(@Param("vendorId") UUID vendorId);

  // Hard delete - for cleanup, not normal operations
  void deleteByVendorId(UUID vendorId);
}
