/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import cm.jemil.proxi.gaz.application.outbound.Clock;
import cm.jemil.proxi.gaz.application.outbound.Configurations;
import cm.jemil.proxi.gaz.application.outbound.DemoRepository;
import cm.jemil.proxi.gaz.application.outbound.UserRepository;
import cm.jemil.proxi.gaz.application.outbound.VendorOnboardingRepository;
import cm.jemil.proxi.gaz.application.outbound.VendorRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
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
  UserRepository userRepository(UserSpringRepository userSpringRepository, JpaMapper jpaMapper) {
    return new JpaUserRepository(userSpringRepository, jpaMapper);
  }

  @Bean
  VendorRepository vendorRepository(
      VendorSpringRepository vendorSpringRepository, JpaMapper jpaMapper) {
    return new JpaVendorRepository(vendorSpringRepository, jpaMapper);
  }

  @Bean
  VendorOnboardingRepository vendorOnboardingRepository(
      UserSpringRepository userSpringRepository,
      VendorSpringRepository vendorSpringRepository,
      VendorSessionSpringRepository vendorSessionSpringRepository,
      JpaMapper jpaMapper) {
    return new JpaVendorOnboardingRepository(
        userSpringRepository, vendorSpringRepository, vendorSessionSpringRepository, jpaMapper);
  }

  @Bean
  @ConditionalOnProperty(
      prefix = "proxigaz.admin.bootstrap",
      name = "enabled",
      havingValue = "true")
  ApplicationRunner adminUserBootstrapRunner(
      UserRepository userRepository,
      Configurations configurations,
      PasswordEncoder passwordEncoder,
      Clock clock) {
    return new AdminUserBootstrapRunner(userRepository, configurations, passwordEncoder, clock);
  }
}
