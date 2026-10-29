/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.user;

import static org.assertj.core.api.Assertions.assertThat;

import cm.jemil.proxi.gaz.domain.shared.CreatedAt;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.shared.PasswordHash;
import java.time.Instant;
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
}
