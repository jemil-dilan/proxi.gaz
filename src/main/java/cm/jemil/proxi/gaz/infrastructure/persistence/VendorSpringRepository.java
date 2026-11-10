/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import cm.jemil.proxi.gaz.application.views.VendorView.VendorView1;
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
      value =
          """
              SELECT v.id                  AS vendorId,
                     v.userId              AS userId,
                     v.name                AS name,
                     v.phone               AS phone,
                     v.location            AS location,
                     v.addressText         AS addressText,
                     v.station             AS isStation,
                     v.deliveryFeeType     AS deliveryFeeType,
                     v.deliveryFeeAmount   AS deliveryFeeAmount,
                     v.active              AS isActive,
                     v.createdAt           AS createdAt,
                     v.updatedAt           AS updatedAt,
                     u.email               AS email,
                     CASE WHEN vs_latest.id IS NOT NULL THEN true ELSE false END AS hasActiveSession,
                     vs_latest.createdAt   AS sessionCreatedAt,
                     vs_latest.lastUsedAt   AS sessionLastUsedAt
              FROM VendorJpa v
              LEFT JOIN UserJpa u ON u.id = v.userId
              LEFT JOIN VendorSessionJpa vs_latest ON vs_latest.vendorId = v.id AND vs_latest.createdAt = (
                  SELECT MAX(vs2.createdAt)
                  FROM VendorSessionJpa vs2
                  WHERE vs2.vendorId = v.id AND vs2.revokedAt IS NULL
              )
              WHERE (:active is null or v.active = :active)
              AND (:name IS NULL OR LOWER(v.name) LIKE LOWER(CONCAT('%', :name, '%')))
          """,
      countQuery =
          """
              SELECT COUNT(v.id)
              FROM VendorJpa v
              WHERE (:active is null or v.active = :active)
              AND (:name IS NULL OR LOWER(v.name) LIKE LOWER(CONCAT('%', :name, '%')))
          """)
  Page<VendorView1> loadAllVendorView(
      @Param("active") Boolean active, @Param("name") String name, Pageable pageable);

  @Query(
      value =
          """
              SELECT v.id                  AS vendorId,
                     v.userId              AS userId,
                     v.name                AS name,
                     v.phone               AS phone,
                     v.location            AS location,
                     v.addressText         AS addressText,
                     v.station             AS isStation,
                     v.deliveryFeeType     AS deliveryFeeType,
                     v.deliveryFeeAmount   AS deliveryFeeAmount,
                     v.active              AS isActive,
                     v.createdAt           AS createdAt,
                     v.updatedAt           AS updatedAt,
                     u.email               AS email,
                     CASE WHEN vs_latest.id IS NOT NULL THEN true ELSE false END AS hasActiveSession,
                     vs_latest.createdAt   AS sessionCreatedAt,
                     vs_latest.lastUsedAt   AS sessionLastUsedAt
              FROM VendorJpa v
              LEFT JOIN UserJpa u ON u.id = v.userId
              LEFT JOIN VendorSessionJpa vs_latest ON vs_latest.vendorId = v.id AND vs_latest.createdAt = (
                  SELECT MAX(vs2.createdAt)
                  FROM VendorSessionJpa vs2
                  WHERE vs2.vendorId = v.id AND vs2.revokedAt IS NULL
              )
              WHERE v.id = :vendorId
          """)
  Optional<VendorView1> findVendorViewById(@Param("vendorId") UUID vendorId);
}
