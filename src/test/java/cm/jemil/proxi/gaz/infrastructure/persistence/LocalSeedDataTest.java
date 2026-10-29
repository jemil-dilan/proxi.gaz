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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@Tag("data")
@SpringBootTest
@ActiveProfiles("data_test")
class LocalSeedDataTest {

  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private PasswordEncoder passwordEncoder;

  @Test
  void localSeed_shouldCreateDevelopmentAdminAndFiveDoualaVendors() {
    Integer adminCount =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM t_admin_users WHERE c_email = 'admin@proxigaz.local'",
            Integer.class);
    Integer vendorCount =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM t_vendors WHERE c_id::text LIKE '01100000-%'", Integer.class);
    Integer vendorsInCentralDouala =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*) FROM t_vendors
            WHERE c_id::text LIKE '01100000-%'
              AND ST_X(c_location::geometry) BETWEEN 9.68 AND 9.75
              AND ST_Y(c_location::geometry) BETWEEN 4.00 AND 4.08
            """,
            Integer.class);
    String passwordHash =
        jdbcTemplate.queryForObject(
            "SELECT c_password_hash FROM t_admin_users WHERE c_email = 'admin@proxigaz.local'",
            String.class);

    assertThat(adminCount).isEqualTo(1);
    assertThat(passwordEncoder.matches("password", passwordHash)).isTrue();
    assertThat(vendorCount).isBetween(5, 10);
    assertThat(vendorsInCentralDouala).isEqualTo(vendorCount);
  }
}
