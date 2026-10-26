/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import cm.jemil.proxi.gaz.application.outbound.AdminUserRepository;
import cm.jemil.proxi.gaz.application.outbound.Clock;
import cm.jemil.proxi.gaz.application.outbound.Configurations;
import cm.jemil.proxi.gaz.config.ProxigazProperties.Admin.Bootstrap;
import cm.jemil.proxi.gaz.domain.admin.AdminUser;
import cm.jemil.proxi.gaz.domain.shared.CreatedAt;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.shared.PasswordHash;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class AdminUserBootstrapRunner implements ApplicationRunner {

  private final AdminUserRepository adminUserRepository;
  private final Configurations configurations;
  private final PasswordEncoder passwordEncoder;
  private final Clock clock;

  @Override
  public void run(ApplicationArguments args) {
    if (!StringUtils.hasText(configurations.bootstrapEmail())
        || !StringUtils.hasText(configurations.bootstrapPassword())) {
      throw new IllegalStateException(
          "Admin bootstrap requires proxigaz.admin.bootstrap.email and password");
    }

    var email = Email.from(configurations.bootstrapEmail());
    if (adminUserRepository.existsByEmail(email)) {
      return;
    }

    var adminUser =
        AdminUser.of(
            email,
            PasswordHash.from(passwordEncoder.encode(configurations.bootstrapPassword())),
            CreatedAt.from(clock.now()));
    adminUserRepository.insert(adminUser);
  }
}
