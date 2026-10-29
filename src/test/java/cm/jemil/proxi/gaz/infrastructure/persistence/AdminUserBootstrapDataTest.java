/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import cm.jemil.proxi.gaz.application.outbound.UserRepository;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.user.UserRole;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@Tag("data")
@SpringBootTest(
    properties = {
      "proxigaz.admin.bootstrap.enabled=true",
      "proxigaz.admin.bootstrap.email=bootstrap-admin@example.test",
      "proxigaz.admin.bootstrap.password=PXG009-test-only-password"
    })
@ActiveProfiles("data_test")
class AdminUserBootstrapDataTest {

  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  @Test
  void bootstrap_shouldPersistAdminWithEncodedPassword() {
    var admin =
        userRepository.findByEmail(Email.from("bootstrap-admin@example.test")).orElseThrow();

    assertThat(admin.getPasswordHash().value()).isNotEqualTo("PXG009-test-only-password");
    assertThat(admin.roles()).containsExactly(UserRole.ADMIN);
    assertThat(
            passwordEncoder.matches("PXG009-test-only-password", admin.getPasswordHash().value()))
        .isTrue();
  }
}
