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
@Table(name = "t_vendor_sessions")
class VendorSessionJpa {

  @Id
  @Column(name = "c_id", nullable = false)
  private UUID id;

  @Column(name = "c_vendor_id", nullable = false)
  private UUID vendorId;

  @Column(name = "c_token_hash", nullable = false, unique = true)
  private String tokenHash;

  @Column(name = "c_created_at", nullable = false)
  private Instant createdAt;
}
