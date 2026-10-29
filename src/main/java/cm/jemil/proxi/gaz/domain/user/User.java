/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.user;

import cm.jemil.proxi.gaz.domain.shared.CreatedAt;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.shared.PasswordHash;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/** Aggregate root for account identity, optional email, credentials, and application roles. */
@Getter
public final class User {

  private final UserId id;
  private final Set<UserRole> roles;
  private @Nullable Email email;
  private @Nullable PasswordHash passwordHash;
  private final CreatedAt createdAt;

  public User(
      UserId id,
      Set<UserRole> roles,
      @Nullable Email email,
      @Nullable PasswordHash passwordHash,
      CreatedAt createdAt) {
    this.id = id;
    this.roles = roles;
    this.email = email;
    this.passwordHash = passwordHash;
    this.createdAt = createdAt;
  }

  public static User createAdmin(Email email, PasswordHash passwordHash, CreatedAt createdAt) {
    return new User(UserId.generate(), Set.of(UserRole.ADMIN), email, passwordHash, createdAt);
  }

  public static User createVendor(@Nullable Email email, CreatedAt createdAt) {
    return new User(UserId.generate(), Set.of(UserRole.VENDOR), email, null, createdAt);
  }

  public static User reconstitute(
      UserId id,
      Set<UserRole> roles,
      @Nullable Email email,
      @Nullable PasswordHash passwordHash,
      CreatedAt createdAt) {
    return new User(id, roles, email, passwordHash, createdAt);
  }

  public UUID id() {
    return id.value();
  }

  public Set<UserRole> roles() {
    return Set.copyOf(roles);
  }

  public Optional<Email> email() {
    return Optional.ofNullable(email);
  }

  public CreatedAt createdAt() {
    return createdAt;
  }

  public boolean isAdmin() {
    return roles.contains(UserRole.ADMIN);
  }

  public boolean isVendor() {
    return roles.contains(UserRole.VENDOR);
  }

  public Set<String> scopes() {
    return roles.stream()
        .map(UserRole::scope)
        .collect(java.util.stream.Collectors.toUnmodifiableSet());
  }

  @Override
  public boolean equals(Object other) {
    return this == other || (other instanceof User user && id.equals(user.id));
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
