/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import cm.jemil.proxi.gaz.application.outbound.AdminUserRepository;
import cm.jemil.proxi.gaz.application.outbound.Clock;
import cm.jemil.proxi.gaz.application.outbound.Configurations;
import cm.jemil.proxi.gaz.application.outbound.DemoRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@EntityScan(basePackages = {"cm.jemil.proxi.gaz.infrastructure.persistence"})
@EnableJpaRepositories(basePackages = {"cm.jemil.proxi.gaz.infrastructure.persistence"})
@Configuration
public class PersistenceBeans {

  @Bean
  DemoRepository demoRepository(DemoSpringRepository demoSpringRepository, JpaMapper jpaMapper) {
    return new JpaDemoRepository(demoSpringRepository, jpaMapper);
  }

  @Bean
  AdminUserRepository adminUserRepository(
      AdminUserSpringRepository adminUserSpringRepository, JpaMapper jpaMapper) {
    return new JpaAdminUserRepository(adminUserSpringRepository, jpaMapper);
  }

  @Bean
  VendorRepository vendorRepository(JdbcTemplate jdbcTemplate, JpaMapper jpaMapper) {
    return new PostgresVendorRepository(jdbcTemplate, jpaMapper);
  }

  @Bean
  @ConditionalOnProperty(
      prefix = "proxigaz.admin.bootstrap",
      name = "enabled",
      havingValue = "true")
  ApplicationRunner adminUserBootstrapRunner(
      AdminUserRepository adminUserRepository,
      Configurations configurations,
      PasswordEncoder passwordEncoder,
      Clock clock) {
    return new AdminUserBootstrapRunner(
        adminUserRepository, configurations, passwordEncoder, clock);
  }
}
