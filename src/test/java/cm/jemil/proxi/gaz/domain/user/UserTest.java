/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cm.jemil.proxi.gaz.domain.shared.CreatedAt;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.shared.PasswordHash;
import java.time.Instant;
import java.util.HashSet;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserTest {

  @Test
  void createAdmin_shouldRequireEmailAndPasswordAndHaveAdminRole() {
    var user =
        User.createAdmin(
            Email.from("admin@example.test"),
            PasswordHash.from("hash"),
            CreatedAt.from(Instant.EPOCH));

    assertThat(user.roles()).containsExactly(UserRole.ADMIN);
    assertThat(user.isAdmin()).isTrue();
    assertThat(user.email()).contains(Email.from("admin@example.test"));
  }

  @Test
  void createVendor_shouldAllowEmailAbsenceAndUseSessionAuthentication() {
    var user = User.createVendor(null, CreatedAt.from(Instant.EPOCH));

    assertThat(user.roles()).containsExactly(UserRole.VENDOR);
    assertThat(user.email()).isEmpty();
    assertThat(user.getPasswordHash()).isNull();
  }

  @Test
  void createVendor_shouldPreserveAnOptionalContactEmail() {
    var email = Email.from("vendor@example.test");

    var user = User.createVendor(email, CreatedAt.from(Instant.EPOCH));

    assertThat(user.email()).contains(email);
    assertThat(user.getPasswordHash()).isNull();
  }

  @Test
  void constructor_shouldDefensivelyCopyRoles() {
    var roles = new HashSet<>(java.util.Set.of(UserRole.VENDOR));
    var user =
        User.reconstitute(
            cm.jemil.proxi.gaz.domain.shared.UserId.from(UUID.randomUUID()),
            roles,
            null,
            null,
            CreatedAt.from(Instant.EPOCH));

    roles.add(UserRole.ADMIN);
    var returnedRoles = user.getRoles();

    assertThat(returnedRoles).containsExactly(UserRole.VENDOR);
    assertThat(user.roles()).containsExactly(UserRole.VENDOR);
  }

  @Test
  void constructor_shouldRejectEmptyRoles() {
    assertThatThrownBy(
            () ->
                User.reconstitute(
                    cm.jemil.proxi.gaz.domain.shared.UserId.from(UUID.randomUUID()),
                    java.util.Set.of(),
                    null,
                    null,
                    CreatedAt.from(Instant.EPOCH)))
        .isInstanceOf(cm.jemil.proxi.gaz.domain.InvalidDomainValueException.class);
  }
}
