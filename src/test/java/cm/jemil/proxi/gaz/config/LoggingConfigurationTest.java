/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

/** SQL bind values contain password hashes, token hashes and phone numbers: local logs only. */
class LoggingConfigurationTest {

  private static final String BIND_LOGGER = "logging.level.org.hibernate.orm.jdbc.bind";

  @ParameterizedTest
  @ValueSource(
      strings = {"application.yaml", "application-staging.yaml", "application-production.yaml"})
  void sharedConfigurations_shouldNeverLogSqlBindValues(String file) throws IOException {
    assertThat(load(file).getProperty(BIND_LOGGER)).isNull();
  }

  @ParameterizedTest
  @ValueSource(strings = {"application.yaml", "application-production.yaml"})
  void sharedConfigurations_shouldNotLogEveryStatement(String file) throws IOException {
    assertThat(load(file).getProperty("logging.level.org.hibernate.SQL")).isNull();
  }

  @Test
  void everyEnvironment_shouldLogSlowQueriesAndTheCorrelationId() throws IOException {
    var base = load("application.yaml");

    assertThat(base.getProperty("spring.jpa.properties.hibernate.log_slow_query")).isNotNull();
    assertThat(String.valueOf(base.getProperty("logging.pattern.correlation")))
        .contains("%X{correlationId");
  }

  @Test
  void localProfile_shouldLogStatementsWithBindValues() throws IOException {
    var local = load("application-local.yaml");

    assertThat(String.valueOf(local.getProperty("logging.level.org.hibernate.SQL")))
        .isEqualTo("DEBUG");
    assertThat(String.valueOf(local.getProperty(BIND_LOGGER))).isEqualTo("TRACE");
  }

  private static PropertySource<?> load(String file) throws IOException {
    return new YamlPropertySourceLoader().load(file, new ClassPathResource(file)).getFirst();
  }
}
