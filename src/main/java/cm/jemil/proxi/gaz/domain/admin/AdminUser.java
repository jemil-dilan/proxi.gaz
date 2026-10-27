/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.admin;

import cm.jemil.proxi.gaz.domain.shared.CreatedAt;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.shared.PasswordHash;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class AdminUser {

  private final UserId id;
  private final Email email;
  private final PasswordHash passwordHash;
  private final CreatedAt createdAt;

  public AdminUser(UserId id, Email email, PasswordHash passwordHash, CreatedAt createdAt) {
    this.id = Objects.requireNonNull(id);
    this.email = Objects.requireNonNull(email);
    this.passwordHash = Objects.requireNonNull(passwordHash);
    this.createdAt = Objects.requireNonNull(createdAt);
  }

  public static AdminUser of(Email email, PasswordHash passwordHash, CreatedAt createdAt) {
    return new AdminUser(UserId.generate(), email, passwordHash, createdAt);
  }

  public static AdminUser reconstitute(
      UserId id, Email email, PasswordHash passwordHash, CreatedAt createdAt) {
    return new AdminUser(id, email, passwordHash, createdAt);
  }

  public UUID id() {
    return id.value();
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof AdminUser adminUser)) {
      return false;
    }
    return id.equals(adminUser.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
