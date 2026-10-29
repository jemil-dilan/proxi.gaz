/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.e2e.steps;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import cm.jemil.proxi.gaz.e2e.HttpClient;
import io.cucumber.datatable.DataTable;
import io.cucumber.java8.En;
import io.restassured.http.ContentType;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;

public class VendorManagementSteps implements En {

  private static final String VENDORS_PATH = "/admin/vendors";

  private final HttpClient httpClient;
  private final JdbcTemplate jdbcTemplate;
  private UUID vendorId;
  private UUID userId;
  private String sessionToken;
  private final List<String> issuedSessionTokens = new ArrayList<>();

  public VendorManagementSteps(HttpClient httpClient, JdbcTemplate jdbcTemplate) {
    this.httpClient = httpClient;
    this.jdbcTemplate = jdbcTemplate;

    When(
        "I create a vendor with the following data",
        (DataTable dataTable) -> {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .header("Authorization", httpClient.authorizationHeader())
                  .body(toCreateRequest(dataTable.asMaps().getFirst()))
                  .post(VENDORS_PATH);
          httpClient.recordResponse(response.statusCode(), response.asString());
          if (response.statusCode() == 201) {
            vendorId = UUID.fromString(response.jsonPath().getString("vendorId"));
            sessionToken = response.jsonPath().getString("sessionToken");
            issuedSessionTokens.add(sessionToken);
          }
        });

    Then(
        "the vendor create request succeeds",
        () -> assertThat(httpClient.getLastResponse().status().value()).isEqualTo(201));

    Then(
        "the created user is linked to the vendor and has a stored session hash",
        () -> {
          userId =
              jdbcTemplate.queryForObject(
                  "SELECT c_user_id FROM t_vendors WHERE c_id = ?", UUID.class, vendorId);
          assertThat(
                  jdbcTemplate.queryForObject(
                      "SELECT COUNT(*) FROM t_users WHERE c_id = ?", Integer.class, userId))
              .isEqualTo(1);
          assertThat(
                  jdbcTemplate.queryForObject(
                      "SELECT COUNT(*) FROM t_user_roles WHERE c_user_id = ? AND c_role = 'VENDOR'",
                      Integer.class,
                      userId))
              .isEqualTo(1);
          assertThat(
                  jdbcTemplate.queryForObject(
                      "SELECT COUNT(*) FROM t_vendors WHERE c_id = ? AND c_user_id = ?",
                      Integer.class,
                      vendorId,
                      userId))
              .isEqualTo(1);
          var storedHash =
              jdbcTemplate.queryForObject(
                  "SELECT c_token_hash FROM t_vendor_sessions WHERE c_vendor_id = ?",
                  String.class,
                  vendorId);
          assertThat(storedHash).isEqualTo(sha256(sessionToken));
          assertThat(storedHash).isNotEqualTo(sessionToken);
        });

    Then(
        "the user and vendor remain linked after update",
        () -> {
          assertThat(
                  jdbcTemplate.queryForObject(
                      "SELECT COUNT(*) FROM t_users WHERE c_id = ?", Integer.class, userId))
              .isEqualTo(1);
          assertThat(
                  jdbcTemplate.queryForObject(
                      "SELECT COUNT(*) FROM t_vendors WHERE c_id = ? AND c_user_id = ?",
                      Integer.class,
                      vendorId,
                      userId))
              .isEqualTo(1);
        });

    Then(
        "the generated vendor session tokens are distinct",
        () -> assertThat(issuedSessionTokens).doesNotHaveDuplicates());

    When(
        "I update that vendor with the following data",
        (DataTable dataTable) -> {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .header("Authorization", httpClient.authorizationHeader())
                  .pathParam("vendorId", vendorId)
                  .body(toUpdateRequest(dataTable.asMaps().getFirst()))
                  .put(VENDORS_PATH + "/{vendorId}");
          httpClient.recordResponse(response.statusCode(), response.asString());
        });

    When(
        "I update a missing vendor with the following data",
        (DataTable dataTable) -> {
          var missingId = UUID.randomUUID();
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .header("Authorization", httpClient.authorizationHeader())
                  .pathParam("vendorId", missingId)
                  .body(toUpdateRequest(dataTable.asMaps().getFirst()))
                  .put(VENDORS_PATH + "/{vendorId}");
          httpClient.recordResponse(response.statusCode(), response.asString());
        });

    Then(
        "the vendor update request succeeds",
        () -> assertThat(httpClient.getLastResponse().status().value()).isEqualTo(204));

    Then(
        "the vendor request is rejected with status {int}",
        (Integer status) ->
            assertThat(httpClient.getLastResponse().status().value())
                .withFailMessage(
                    "Expected status %s but response body was: %s",
                    status, httpClient.getLastResponseBody())
                .isEqualTo(status));

    Then(
        "the vendor is persisted with the following data",
        (DataTable dataTable) -> {
          var expected = dataTable.asMaps().getFirst();
          var actual =
              jdbcTemplate.queryForMap(
                  """
                  SELECT c_name, c_phone, ST_Y(c_location::geometry) AS latitude,
                         ST_X(c_location::geometry) AS longitude, c_address_text,
                         c_is_station, c_delivery_fee_type, c_delivery_fee_amount, c_is_active
                  FROM t_vendors WHERE c_id = ?
                  """,
                  vendorId);

          assertThat(actual.get("c_name")).isEqualTo(expected.get("name"));
          assertThat(actual.get("c_phone")).isEqualTo(expected.get("phone"));
          assertThat(((Number) actual.get("latitude")).doubleValue())
              .isCloseTo(Double.parseDouble(expected.get("latitude")), within(0.000001));
          assertThat(((Number) actual.get("longitude")).doubleValue())
              .isCloseTo(Double.parseDouble(expected.get("longitude")), within(0.000001));
          assertThat(actual.get("c_address_text")).isEqualTo(expected.get("addressText"));
          assertThat(actual.get("c_is_station"))
              .isEqualTo(Boolean.parseBoolean(expected.get("isStation")));
          assertThat(actual.get("c_delivery_fee_type")).isEqualTo(expected.get("deliveryFeeType"));
          assertThat(actual.get("c_delivery_fee_amount"))
              .isEqualTo(Integer.parseInt(expected.get("deliveryFeeAmount")));
          assertThat(actual.get("c_is_active"))
              .isEqualTo(Boolean.parseBoolean(expected.get("isActive")));
        });
  }

  private static Map<String, Object> toCreateRequest(Map<String, String> values) {
    var request = commonRequest(values);
    request.put("address", values.get("addressText"));
    return request;
  }

  private static Map<String, Object> toUpdateRequest(Map<String, String> values) {
    var request = commonRequest(values);
    request.put("addressText", values.get("addressText"));
    return request;
  }

  private static Map<String, Object> commonRequest(Map<String, String> values) {
    var request = new HashMap<String, Object>();
    if (values.containsKey("email")) {
      request.put("email", blankToNull(values.get("email")));
    }
    request.put("name", values.get("name"));
    request.put("phone", values.get("phone"));
    request.put("latitude", parseDouble(values.get("latitude")));
    request.put("longitude", parseDouble(values.get("longitude")));
    request.put("isStation", parseBoolean(values.get("isStation")));
    request.put("deliveryFeeType", blankToNull(values.get("deliveryFeeType")));
    request.put("deliveryFeeAmount", parseInteger(values.get("deliveryFeeAmount")));
    request.put("isActive", parseBoolean(values.get("isActive")));
    return request;
  }

  private static Double parseDouble(String value) {
    return value == null || value.isBlank() ? null : Double.valueOf(value);
  }

  private static Integer parseInteger(String value) {
    return value == null || value.isBlank() ? null : Integer.valueOf(value);
  }

  private static Boolean parseBoolean(String value) {
    return value == null || value.isBlank() ? null : Boolean.valueOf(value);
  }

  private static String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }

  private static String sha256(String value) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is not available", exception);
    }
  }
}
