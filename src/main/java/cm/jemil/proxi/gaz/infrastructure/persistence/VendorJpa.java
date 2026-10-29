/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

@SuppressWarnings("JpaDataSourceORMInspection")
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "t_vendors")
class VendorJpa {

  @Id
  @Column(name = "c_id", nullable = false)
  private UUID id;

  @Column(name = "c_user_id", nullable = false, unique = true)
  private UUID userId;

  @Column(name = "c_name", nullable = false)
  private String name;

  @Column(name = "c_phone", nullable = false)
  private String phone;

  @JdbcTypeCode(SqlTypes.GEOGRAPHY)
  @Column(name = "c_location", nullable = false, columnDefinition = "geography(Point,4326)")
  private Point location;

  @Column(name = "c_address_text")
  private String addressText;

  @Column(name = "c_is_station", nullable = false)
  private boolean station;

  @Column(name = "c_delivery_fee_type")
  private String deliveryFeeType;

  @Column(name = "c_delivery_fee_amount")
  private Integer deliveryFeeAmount;

  @Column(name = "c_is_active", nullable = false)
  private boolean active;

  @Column(name = "c_reliability_score", nullable = false)
  private Integer reliabilityScore = 0;

  @Column(name = "c_created_at", nullable = false, insertable = false, updatable = false)
  private Instant createdAt;
}
