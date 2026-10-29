/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cm.jemil.proxi.gaz.application.outbound.UserRepository;
import cm.jemil.proxi.gaz.domain.shared.CreatedAt;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.shared.PasswordHash;
import cm.jemil.proxi.gaz.domain.user.User;
import cm.jemil.proxi.gaz.domain.user.UserRole;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@Tag("data")
@SpringBootTest
@ActiveProfiles("data_test")
class UserRepositoryDataTest {

  @Autowired private UserRepository repository;

  @Test
  void insert_thenFindByEmail_shouldPersistAdminUserRoleAndCredentials() {
    var email = Email.from("roundtrip-" + UUID.randomUUID() + "@example.test");
    var user = newAdmin(email);

    repository.insert(user);

    assertThat(repository.findAll()).contains(user);
  }

  @Test
  void insert_shouldRejectDuplicateEmail() {
    var email = Email.from("duplicate-" + UUID.randomUUID() + "@example.test");
    repository.insert(newAdmin(email));

    assertThatThrownBy(() -> repository.insert(newAdmin(email)))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void update_shouldUpdateTheExistingUser() {
    var existingEmail = Email.from("before-" + UUID.randomUUID() + "@example.test");
    var updatedEmail = Email.from("after-" + UUID.randomUUID() + "@example.test");
    var existing = newAdmin(existingEmail);
    repository.insert(existing);
    var updated =
        User.reconstitute(
            existing.getId(),
            Set.of(UserRole.ADMIN),
            updatedEmail,
            PasswordHash.from("updated-encoded-password"),
            existing.createdAt());

    repository.update(updated);

    assertThat(repository.findByEmail(updatedEmail)).contains(updated);
    assertThat(repository.findByEmail(existingEmail)).isEmpty();
  }

  @Test
  void deleteById_shouldRemoveTheUser() {
    var user = newAdmin(Email.from("delete-" + UUID.randomUUID() + "@example.test"));
    repository.insert(user);

    repository.deleteById(user.getId());

    assertThat(repository.findById(user.getId())).isEmpty();
  }

  private User newAdmin(Email email) {
    return User.createAdmin(
        email,
        PasswordHash.from("encoded-password"),
        CreatedAt.from(Instant.now().truncatedTo(ChronoUnit.MICROS)));
  }
}
