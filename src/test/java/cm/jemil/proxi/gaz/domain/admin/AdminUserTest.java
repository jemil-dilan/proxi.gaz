/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.admin;

import static org.assertj.core.api.Assertions.assertThat;

import cm.jemil.proxi.gaz.domain.shared.CreatedAt;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.shared.PasswordHash;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AdminUserTest {

  @Test
  void of_shouldKeepValidatedValueObjects() {
    var email = Email.from("admin@example.com");
    var passwordHash = PasswordHash.from("$2a$10$hash-value");

    var createdAt = CreatedAt.from(Instant.EPOCH);
    var adminUser = AdminUser.of(email, passwordHash, createdAt);

    assertThat(adminUser.getEmail()).isEqualTo(email);
    assertThat(adminUser.getPasswordHash()).isEqualTo(passwordHash);
    assertThat(adminUser.getCreatedAt()).isEqualTo(createdAt);
  }

  @Test
  void equality_shouldUseAdminUserId() {
    var id = UserId.from(UUID.randomUUID());
    var first =
        AdminUser.reconstitute(
            id,
            Email.from("first@example.com"),
            PasswordHash.from("hash-1"),
            CreatedAt.from(Instant.EPOCH));
    var second =
        AdminUser.reconstitute(
            id,
            Email.from("second@example.com"),
            PasswordHash.from("hash-2"),
            CreatedAt.from(Instant.EPOCH));

    assertThat(first).isEqualTo(second);
    assertThat(first.hashCode()).isEqualTo(second.hashCode());
  }
}
