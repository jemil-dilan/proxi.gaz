/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@Tag("data")
@SpringBootTest
@ActiveProfiles("data_test")
class PostgisExtensionDataTest {

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void postgisExtension_shouldBeAvailable_afterLiquibase() {
    String version = jdbcTemplate.queryForObject("SELECT PostGIS_Version()", String.class);

    assertThat(version).isNotBlank();

    String point =
        jdbcTemplate.queryForObject("SELECT ST_AsText(ST_MakePoint(11.5, 3.8))", String.class);

    assertThat(point).isEqualTo("POINT(11.5 3.8)");
  }
}
