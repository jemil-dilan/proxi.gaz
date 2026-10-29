/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@Tag("data")
@SpringBootTest
@ActiveProfiles("data_test")
class CoreSchemaDataTest {

  private static final List<String> REQUIRED_TABLES =
      List.of(
          "t_users",
          "t_user_roles",
          "t_vendors",
          "t_vendor_brands",
          "t_vendor_sessions",
          "t_orders",
          "t_order_offers",
          "t_order_events",
          "t_order_reports",
          "t_reviews",
          "t_push_subscriptions");

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void coreTables_shouldExist_afterLiquibase() {
    for (String table : REQUIRED_TABLES) {
      Integer count =
          jdbcTemplate.queryForObject(
              """
              SELECT COUNT(*) FROM information_schema.tables
              WHERE table_schema = 'public' AND table_name = ?
              """,
              Integer.class,
              table);
      assertThat(count).as("table %s", table).isEqualTo(1);
    }
  }

  @Test
  void vendorsLocation_shouldHaveGistIndex() {
    Integer count =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*) FROM pg_indexes
            WHERE schemaname = 'public'
              AND tablename = 't_vendors'
              AND indexdef ILIKE '%USING gist%'
              AND indexdef ILIKE '%c_location%'
            """,
            Integer.class);

    assertThat(count).isGreaterThanOrEqualTo(1);
  }

  @Test
  void vendorsLocation_shouldAcceptGeographyPoint() {
    jdbcTemplate.update(
        "INSERT INTO t_users (c_id, c_created_at) VALUES (?, NOW())",
        java.util.UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
    jdbcTemplate.update(
        "INSERT INTO t_user_roles (c_user_id, c_role) VALUES (?, 'VENDOR')",
        java.util.UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
    jdbcTemplate.update(
        """
        INSERT INTO t_vendors (
          c_id, c_user_id, c_name, c_phone, c_location, c_address_text, c_is_station,
          c_delivery_fee_type, c_delivery_fee_amount, c_is_active, c_reliability_score
        ) VALUES (
          'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
          'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
          'Schema Smoke Vendor',
          '+237600000001',
          ST_SetSRID(ST_MakePoint(11.5021, 3.8480), 4326)::geography,
          'Pilot neighborhood',
          false,
          'flat',
          500,
          true,
          0
        )
        """);

    Boolean within =
        jdbcTemplate.queryForObject(
            """
            SELECT ST_DWithin(
              c_location,
              ST_SetSRID(ST_MakePoint(11.5021, 3.8480), 4326)::geography,
              100
            )
            FROM t_vendors
            WHERE c_user_id = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'
            """,
            Boolean.class);

    assertThat(within).isTrue();
  }
}
