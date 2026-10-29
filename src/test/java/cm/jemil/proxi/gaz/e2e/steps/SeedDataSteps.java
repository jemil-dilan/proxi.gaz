/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.e2e.steps;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.datatable.DataTable;
import io.cucumber.java8.En;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

public class SeedDataSteps implements En {

  public SeedDataSteps(JdbcTemplate jdbcTemplate) {
    Then(
        "I assume the following vendors are present in the database",
        (DataTable dataTable) -> {
          List<SeedVendor> actual =
              jdbcTemplate.query(
                  """
                  SELECT c_name, c_phone, ST_Y(c_location::geometry) AS latitude,
                    ST_X(c_location::geometry) AS longitude, c_address_text, c_is_station,
                    c_delivery_fee_type, c_delivery_fee_amount, c_is_active
                  FROM t_vendors
                  WHERE c_id::text LIKE '01100000-%'
                  """,
                  (rs, rowNum) ->
                      new SeedVendor(
                          rs.getString("c_name"),
                          rs.getString("c_phone"),
                          rs.getDouble("latitude"),
                          rs.getDouble("longitude"),
                          rs.getString("c_address_text"),
                          rs.getBoolean("c_is_station"),
                          rs.getString("c_delivery_fee_type"),
                          rs.getInt("c_delivery_fee_amount"),
                          rs.getBoolean("c_is_active")));

          List<SeedVendor> expected =
              dataTable.asMaps().stream()
                  .map(
                      row ->
                          new SeedVendor(
                              row.get("name"),
                              row.get("phone"),
                              Double.parseDouble(row.get("latitude")),
                              Double.parseDouble(row.get("longitude")),
                              row.get("addressText"),
                              Boolean.parseBoolean(row.get("isStation")),
                              row.get("deliveryFeeType"),
                              Integer.parseInt(row.get("deliveryFeeAmount")),
                              Boolean.parseBoolean(row.get("isActive"))))
                  .toList();

          assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
        });
  }

  private record SeedVendor(
      String name,
      String phone,
      double latitude,
      double longitude,
      String addressText,
      boolean station,
      String deliveryFeeType,
      int deliveryFeeAmount,
      boolean active) {}
}
