/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cm.jemil.proxi.gaz.application.outbound.AdminUserRepository;
import cm.jemil.proxi.gaz.domain.admin.AdminUser;
import cm.jemil.proxi.gaz.domain.shared.CreatedAt;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.shared.PasswordHash;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
class AdminUserRepositoryDataTest {

  @Autowired private AdminUserRepository adminUserRepository;

  @Test
  void insert_thenFindByEmail_shouldPersistAdminUser() {
    var email = Email.from("roundtrip-" + UUID.randomUUID() + "@example.test");
    var admin = newAdmin(email);

    adminUserRepository.insert(admin);

    assertThat(adminUserRepository.findByEmail(email)).contains(admin);
    assertThat(adminUserRepository.findAll()).contains(admin);
  }

  @Test
  void insert_shouldRejectDuplicateEmail() {
    var email = Email.from("duplicate-" + UUID.randomUUID() + "@example.test");
    adminUserRepository.insert(newAdmin(email));

    assertThatThrownBy(() -> adminUserRepository.insert(newAdmin(email)))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void update_shouldUpdateTheExistingAdmin() {
    var existingEmail = Email.from("before-" + UUID.randomUUID() + "@example.test");
    var updatedEmail = Email.from("after-" + UUID.randomUUID() + "@example.test");
    var existingAdmin = newAdmin(existingEmail);
    adminUserRepository.insert(existingAdmin);
    var updatedAdmin =
        AdminUser.reconstitute(
            existingAdmin.getId(),
            updatedEmail,
            PasswordHash.from("updated-encoded-password"),
            existingAdmin.getCreatedAt());

    adminUserRepository.update(updatedAdmin);

    assertThat(adminUserRepository.findByEmail(existingEmail)).isEmpty();
    assertThat(adminUserRepository.findByEmail(updatedEmail)).contains(updatedAdmin);
  }

  @Test
  void deleteById_shouldRemoveTheAdmin() {
    var admin = newAdmin(Email.from("delete-" + UUID.randomUUID() + "@example.test"));
    adminUserRepository.insert(admin);

    adminUserRepository.deleteById(admin.getId());

    assertThat(adminUserRepository.findById(admin.getId())).isEmpty();
  }

  private AdminUser newAdmin(Email email) {
    return AdminUser.of(
        email,
        PasswordHash.from("encoded-password"),
        CreatedAt.from(Instant.now().truncatedTo(ChronoUnit.MICROS)));
  }
}
