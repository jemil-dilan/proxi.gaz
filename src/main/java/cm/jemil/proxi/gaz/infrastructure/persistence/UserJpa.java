/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import cm.jemil.proxi.gaz.domain.user.UserRole;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@SuppressWarnings("JpaDataSourceORMInspection")
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "t_users")
class UserJpa {

  @Id
  @Column(name = "c_id", nullable = false)
  private UUID id;

  @Column(name = "c_email", unique = true)
  private String email;

  @Column(name = "c_password_hash")
  private String passwordHash;

  @Column(name = "c_created_at", nullable = false)
  private Instant createdAt;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "t_user_roles", joinColumns = @JoinColumn(name = "c_user_id"))
  @Column(name = "c_role", nullable = false)
  @Enumerated(EnumType.STRING)
  private Set<UserRole> roles = new HashSet<>();

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    UserJpa userJpa = (UserJpa) o;
    return Objects.equals(id, userJpa.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
