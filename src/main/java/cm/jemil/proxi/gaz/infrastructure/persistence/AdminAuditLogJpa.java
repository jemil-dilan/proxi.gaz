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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Append-only row: {@link Immutable} makes Hibernate ignore any update of a loaded entry. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Immutable
@Table(name = "t_admin_audit_log")
class AdminAuditLogJpa {

  @Id
  @Column(name = "c_id", nullable = false)
  private UUID id;

  @Column(name = "c_admin_user_id", nullable = false)
  private UUID adminUserId;

  @Column(name = "c_action", nullable = false)
  private String action;

  @Column(name = "c_target_type", nullable = false)
  private String targetType;

  @Column(name = "c_target_id", nullable = false)
  private UUID targetId;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "c_metadata", nullable = false, columnDefinition = "jsonb")
  private Map<String, Object> metadata = new HashMap<>();

  @Column(name = "c_created_at", nullable = false)
  private Instant createdAt;

  /** Database-generated insertion order; read-only, used to break created_at ties. */
  @Column(name = "c_seq", insertable = false, updatable = false)
  private Long seq;
}
