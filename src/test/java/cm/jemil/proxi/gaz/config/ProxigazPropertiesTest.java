/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class ProxigazPropertiesTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner().withUserConfiguration(PropertiesConfiguration.class);

  @Test
  void bindsSecurityAndAdminBootstrapFromSingleRoot() {
    contextRunner
        .withPropertyValues(
            "proxigaz.security.jwt.secret=test-jwt-secret",
            "proxigaz.security.jwt.access-token-ttl=21m",
            "proxigaz.admin.bootstrap.enabled=true",
            "proxigaz.admin.bootstrap.email=admin@example.test",
            "proxigaz.admin.bootstrap.password=test-password")
        .run(
            context -> {
              var properties = context.getBean(ProxigazProperties.class);

              assertThat(properties.security().jwt().secret()).isEqualTo("test-jwt-secret");
              assertThat(properties.security().jwt().accessTokenTtl())
                  .isEqualTo(Duration.ofMinutes(21));
              assertThat(properties.admin().bootstrap().enabled()).isTrue();
              assertThat(properties.admin().bootstrap().email()).isEqualTo("admin@example.test");
              assertThat(properties.admin().bootstrap().password()).isEqualTo("test-password");
            });
  }

  @Test
  void missingBootstrapSettingsDefaultToDisabled() {
    contextRunner.run(
        context -> {
          var properties = context.getBean(ProxigazProperties.class);

          assertThat(properties.admin().bootstrap().enabled()).isFalse();
          assertThat(properties.admin().bootstrap().email()).isNull();
          assertThat(properties.admin().bootstrap().password()).isNull();
          assertThat(properties.security().jwt().accessTokenTtl())
              .isEqualTo(Duration.ofMinutes(15));
        });
  }

  @Configuration(proxyBeanMethods = false)
  @EnableConfigurationProperties(ProxigazProperties.class)
  static class PropertiesConfiguration {}
}
