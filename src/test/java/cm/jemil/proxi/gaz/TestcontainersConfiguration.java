/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

  /** Pinned PostGIS image — keep in sync with CI and jdbc:tc URLs. */
  static final String POSTGIS_IMAGE = "postgis/postgis:17-3.5";

  @Bean
  @ServiceConnection
  PostgreSQLContainer postgresContainer() {
    return new PostgreSQLContainer(
        DockerImageName.parse(POSTGIS_IMAGE).asCompatibleSubstituteFor("postgres"));
  }
}
