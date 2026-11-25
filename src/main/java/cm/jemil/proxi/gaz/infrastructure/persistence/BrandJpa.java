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

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "t_brands")
class BrandJpa {

  @Id
  @Column(name = "c_id", nullable = false)
  private UUID id;

  @Column(name = "c_name", nullable = false, length = 64)
  private String name;

  @Column(name = "c_is_active", nullable = false)
  private boolean active;

  @Column(name = "c_created_at", nullable = false)
  private Instant createdAt;
}
