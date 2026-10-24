/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import cm.jemil.proxi.gaz.application.outbound.AdminUserRepository;
import cm.jemil.proxi.gaz.application.outbound.Clock;
import cm.jemil.proxi.gaz.config.AdminBootstrapProperties;
import cm.jemil.proxi.gaz.domain.admin.AdminUser;
import cm.jemil.proxi.gaz.domain.shared.CreatedAt;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.shared.PasswordHash;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@ConditionalOnProperty(prefix = "proxigaz.admin.bootstrap", name = "enabled", havingValue = "true")
public class AdminUserBootstrapRunner implements ApplicationRunner {

  private final AdminUserRepository adminUserRepository;
  private final AdminBootstrapProperties properties;
  private final PasswordEncoder passwordEncoder;
  private final Clock clock;

  public AdminUserBootstrapRunner(
      AdminUserRepository adminUserRepository,
      AdminBootstrapProperties properties,
      PasswordEncoder passwordEncoder,
      Clock clock) {
    this.adminUserRepository = adminUserRepository;
    this.properties = properties;
    this.passwordEncoder = passwordEncoder;
    this.clock = clock;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (!StringUtils.hasText(properties.email()) || !StringUtils.hasText(properties.password())) {
      throw new IllegalStateException(
          "Admin bootstrap requires proxigaz.admin.bootstrap.email and password");
    }

    var email = Email.from(properties.email());
    if (adminUserRepository.existsByEmail(email)) {
      return;
    }

    var adminUser =
        AdminUser.of(
            email,
            PasswordHash.from(passwordEncoder.encode(properties.password())),
            CreatedAt.from(clock.now()));
    adminUserRepository.insert(adminUser);
  }
}
