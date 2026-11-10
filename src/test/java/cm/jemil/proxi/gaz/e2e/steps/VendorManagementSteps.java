/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.e2e.steps;

import static io.restassured.RestAssured.given;
import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import cm.jemil.proxi.gaz.e2e.HttpClient;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.VendorDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.VendorOnboardingResponseDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.VendorSessionIdentityDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.VendorSessionTokenDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.VendorStatusUpdateDTO;
import io.cucumber.datatable.DataTable;
import io.cucumber.java8.En;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;

public class VendorManagementSteps implements En {

  private static final String VENDORS_PATH = "/admin/vendors";

  private final HttpClient httpClient;
  private VendorOnboardingResponseDTO vendorOnboardingResponseDTO;
  private VendorSessionIdentityDTO vendorSessionIdentityDTO;
  private VendorSessionTokenDTO vendorSessionTokenDTO;
  private VendorDTO vendorDTO;
  private UUID userId;
  private String sessionToken;
  private final List<String> issuedSessionTokens = new ArrayList<>();

  public VendorManagementSteps(HttpClient httpClient, JdbcTemplate jdbcTemplate) {
    this.httpClient = httpClient;

    Given(
        "I assume that the vendors with the following data are inside the database",
        (DataTable dataTable) -> {
          dataTable
              .asMaps()
              .forEach(
                  row -> {
                    var sql =
                        """
                        INSERT INTO t_users (c_id, c_email, c_password_hash, c_created_at)
                        VALUES (?, NULL, NULL, NOW())
                        ON CONFLICT (c_id) DO NOTHING
                        """;
                    var userId = UUID.randomUUID();
                    jdbcTemplate.update(sql, userId);

                    sql =
                        """
                        INSERT INTO t_user_roles (c_user_id, c_role)
                        VALUES (?, 'VENDOR')
                        ON CONFLICT (c_user_id, c_role) DO NOTHING
                        """;
                    jdbcTemplate.update(sql, userId);

                    sql =
                        """
                        INSERT INTO t_vendors (c_id, c_user_id, c_name, c_phone, c_location, c_address_text,
                                               c_is_station, c_delivery_fee_type, c_delivery_fee_amount, c_is_active, c_created_at)
                        VALUES (?, ?, ?, ?, ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography, ?, ?, ?, ?, ?, NOW())
                        ON CONFLICT (c_id) DO NOTHING
                        """;
                    var vendorId = UUID.fromString(row.get("id"));
                    jdbcTemplate.update(
                        sql,
                        vendorId,
                        userId,
                        row.get("name"),
                        row.get("phone"),
                        Double.parseDouble(row.get("longitude")),
                        Double.parseDouble(row.get("latitude")),
                        row.get("addressText"),
                        Boolean.parseBoolean(row.get("isStation")),
                        row.get("deliveryFeeType"),
                        Integer.parseInt(row.get("deliveryFeeAmount")),
                        Boolean.parseBoolean(row.get("isActive")));
                  });
        });

    Given(
        "I assume that the vendors with the following data are not inside the database",
        (DataTable dataTable) -> {
          dataTable
              .asMaps()
              .forEach(
                  row -> {
                    var vendorId = UUID.fromString(row.get("id"));
                    jdbcTemplate.update("DELETE FROM t_vendors WHERE c_id = ?", vendorId);
                    jdbcTemplate.update(
                        "DELETE FROM t_users WHERE c_id IN (SELECT c_user_id FROM t_vendors WHERE"
                            + " c_id = ?)",
                        vendorId);
                  });
        });

    When(
        "I create a vendor with the following data",
        (DataTable dataTable) -> {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .header("Authorization", httpClient.authorizationHeader())
                  .body(toCreateRequest(dataTable.asMaps().getFirst()))
                  .post(VENDORS_PATH);
          assertThat(response.statusCode()).isEqualTo(201);
          httpClient.recordResponse(
              response.statusCode(),
              response.asString(),
              response.headers().asList().stream()
                  .collect(toMap(h -> h.getName(), h -> h.getValue(), (a, b) -> a)));
          this.vendorOnboardingResponseDTO = response.as(VendorOnboardingResponseDTO.class);
          issuedSessionTokens.add(vendorOnboardingResponseDTO.getSessionToken());
        });

    When(
        "I try to create a vendor with the following data",
        (DataTable dataTable) -> {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .header("Authorization", httpClient.authorizationHeader())
                  .body(toCreateRequest(dataTable.asMaps().getFirst()))
                  .post(VENDORS_PATH);
          httpClient.recordResponse(response.statusCode(), response.asString());
        });

    When(
        "I get vendor by id {string}",
        (String vendorId) -> {
          var response =
              given()
                  .header("Authorization", httpClient.authorizationHeader())
                  .pathParam("vendorId", vendorId)
                  .get(VENDORS_PATH + "/{vendorId}");
          httpClient.recordResponse(response.statusCode(), response.asString());
        });

    When(
        "I get vendor by id using the created vendor id",
        () -> {
          this.vendorDTO =
              given()
                  .header("Authorization", httpClient.authorizationHeader())
                  .pathParam("vendorId", this.vendorOnboardingResponseDTO.getVendorId())
                  .get(VENDORS_PATH + "/{vendorId}")
                  .then()
                  .statusCode(200)
                  .extract()
                  .as(VendorDTO.class);
        });

    When(
        "I try to get vendor by id {string}",
        (String vendorId) -> {
          var response =
              given()
                  .header("Authorization", httpClient.authorizationHeader())
                  .pathParam("vendorId", vendorId)
                  .get(VENDORS_PATH + "/{vendorId}");
          httpClient.recordResponse(response.statusCode(), response.asString());
        });

    Then(
        "the vendor detail response contains active session metadata",
        () -> {
          assertThat(this.vendorDTO.getHasActiveSession()).isTrue();
          assertThat(this.vendorDTO.getSessionCreatedAt()).isNotNull();
          assertThat(this.vendorDTO.getSessionLastUsedAt()).isNull();
        });

    Then(
        "the vendor detail response contains revoked session metadata",
        () -> {
          assertThat(this.vendorDTO.getHasActiveSession()).isFalse();
          assertThat(this.vendorDTO.getSessionCreatedAt()).isNull();
          assertThat(this.vendorDTO.getSessionLastUsedAt()).isNull();
        });

    Then(
        "the vendor detail response contains used session metadata",
        () -> {
          assertThat(this.vendorDTO.getHasActiveSession()).isTrue();
          assertThat(this.vendorDTO.getSessionCreatedAt()).isNotNull();
          assertThat(this.vendorDTO.getSessionLastUsedAt()).isNotNull();
        });

    Then(
        "the vendor detail isActive is {word}",
        (String isActive) ->
            assertThat(this.vendorDTO.getIsActive()).isEqualTo(Boolean.parseBoolean(isActive)));

    Then(
        "the session has active session",
        () -> {
          assertThat(this.vendorDTO.getHasActiveSession()).isTrue();
        });

    Then(
        "the vendor list is sorted by createdAt ascending",
        () -> {
          var body = JsonPath.from(httpClient.getLastResponseBody());
          var createdAt = body.getList("content.createdAt", String.class);
          assertThat(createdAt).isSorted();
        });

    Then(
        "the vendor session contains name {string}",
        (String expectedName) -> {
          assertThat(this.vendorSessionIdentityDTO.getName()).isEqualTo(expectedName);
        });

    Then(
        "the vendor session contains isActive {word}",
        (String isActive) -> {
          assertThat(this.vendorSessionIdentityDTO.getIsActive())
              .isEqualTo(Boolean.parseBoolean(isActive));
        });

    Then(
        "the vendor create response has Cache-Control no-store header",
        () -> {
          assertThat(httpClient.headerContains("Cache-Control", "no-store")).isTrue();
        });

    Then(
        "the vendor list is sorted by name ascending",
        () -> {
          var body = JsonPath.from(httpClient.getLastResponseBody());
          var names = body.getList("content.name", String.class);
          assertThat(names).isSortedAccordingTo(String.CASE_INSENSITIVE_ORDER);
        });

    When(
        "I try create a vendor with invalid data",
        () -> {
          var request = Map.of("name", "", "phone", "");
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .header("Authorization", httpClient.authorizationHeader())
                  .body(request)
                  .post(VENDORS_PATH);
          httpClient.recordResponse(response.statusCode(), response.asString());
        });

    When(
        "I try to create vendor with duplicate phone {string}",
        (String phone) -> {
          var request =
              Map.of(
                  "name", "Duplicate Vendor", "phone", phone, "latitude", 4.05, "longitude", 9.7);
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .header("Authorization", httpClient.authorizationHeader())
                  .body(request)
                  .post(VENDORS_PATH);
          httpClient.recordResponse(response.statusCode(), response.asString());
        });

    When(
        "I list vendors at page {int} with size {int}, active {string}, and name {string}",
        (Integer page, Integer size, String active, String name) -> {
          listVendors(page, size, active, name, null);
        });

    When(
        "I list vendors at page {int} with size {int}, active {string}, name {string}, and sort"
            + " {string}",
        (Integer page, Integer size, String active, String name, String sort) -> {
          listVendors(page, size, active, name, sort);
        });

    Then(
        "the vendor page metadata is page {int}, size {int}, total elements {int}, total pages"
            + " {int}",
        (Integer page, Integer size, Integer totalElements, Integer totalPages) -> {
          var response = httpClient.getLastResponse();
          var body = JsonPath.from(httpClient.getLastResponseBody());
          assertThat(response.status().value()).isEqualTo(200);
          assertThat(body.getInt("page")).isEqualTo(page);
          assertThat(body.getInt("size")).isEqualTo(size);
          assertThat(body.getInt("totalElements")).isEqualTo(totalElements);
          assertThat(body.getInt("totalPages")).isEqualTo(totalPages);
        });

    Then(
        "the vendor list contains only the name {string}",
        (String name) ->
            assertThat(
                    JsonPath.from(httpClient.getLastResponseBody())
                        .getList("content.name", String.class))
                .containsExactly(name));

    Then(
        "the created user is linked to the vendor and has a stored session hash",
        () -> {
          userId =
              jdbcTemplate.queryForObject(
                  "SELECT c_user_id FROM t_vendors WHERE c_id = ?",
                  UUID.class,
                  this.vendorOnboardingResponseDTO.getVendorId());
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
                      this.vendorOnboardingResponseDTO.getVendorId(),
                      userId))
              .isEqualTo(1);
          var storedHash =
              jdbcTemplate.queryForObject(
                  "SELECT c_token_hash FROM t_vendor_sessions WHERE c_vendor_id = ?",
                  String.class,
                  this.vendorOnboardingResponseDTO.getVendorId());
          assertThat(storedHash)
              .isEqualTo(sha256(this.vendorOnboardingResponseDTO.getSessionToken()));
          assertThat(storedHash).isNotEqualTo(this.vendorOnboardingResponseDTO.getSessionToken());
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
                      this.vendorOnboardingResponseDTO.getVendorId(),
                      userId))
              .isEqualTo(1);
        });

    Then(
        "the generated vendor session tokens are distinct",
        () -> assertThat(issuedSessionTokens).doesNotHaveDuplicates());

    When(
        "I access the vendor session using the issued token",
        () -> {
          this.vendorSessionIdentityDTO =
              given()
                  .header("Authorization", "Bearer " + currentSessionToken())
                  .get("/vendor/session")
                  .then()
                  .statusCode(200)
                  .extract()
                  .as(new TypeRef<>() {});
        });

    When(
        "I try to access the vendor session using the previous token",
        () -> {
          assertThat(issuedSessionTokens)
              .as("expected at least two issued tokens after rotation")
              .hasSizeGreaterThanOrEqualTo(2);
          var previousToken = issuedSessionTokens.get(issuedSessionTokens.size() - 2);
          var response =
              given().header("Authorization", "Bearer " + previousToken).get("/vendor/session");
          httpClient.recordResponse(response.statusCode(), response.asString());
        });

    When(
        "I try to access the vendor session using the issued token",
        () -> {
          var response =
              given()
                  .header("Authorization", "Bearer " + currentSessionToken())
                  .get("/vendor/session");
          httpClient.recordResponse(response.statusCode(), response.asString());
        });

    When(
        "I rotate the vendor session as admin",
        () -> {
          this.vendorSessionTokenDTO =
              given()
                  .header("Authorization", httpClient.authorizationHeader())
                  .pathParam("vendorId", this.vendorOnboardingResponseDTO.getVendorId())
                  .post(VENDORS_PATH + "/{vendorId}/session")
                  .then()
                  .statusCode(201)
                  .extract()
                  .as(VendorSessionTokenDTO.class);
          issuedSessionTokens.add(this.vendorSessionTokenDTO.getSessionToken());
        });

    When(
        "I revoke the vendor session as admin",
        () ->
            given()
                .header("Authorization", httpClient.authorizationHeader())
                .pathParam("vendorId", this.vendorOnboardingResponseDTO.getVendorId())
                .delete(VENDORS_PATH + "/{vendorId}/session")
                .then()
                .statusCode(204));

    When(
        "I update that vendor with the following data",
        (DataTable dataTable) -> {
          given()
              .contentType(ContentType.JSON)
              .header("Authorization", httpClient.authorizationHeader())
              .pathParam("vendorId", this.vendorOnboardingResponseDTO.getVendorId())
              .body(toUpdateRequest(dataTable.asMaps().getFirst()))
              .put(VENDORS_PATH + "/{vendorId}")
              .then()
              .statusCode(204);
        });

    When(
        "I try to update a vendor with the following data",
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

    When(
        "I set that vendor's active status to {string}",
        (String active) -> {
          var vendorUpdateDTO = new VendorStatusUpdateDTO(Boolean.parseBoolean(active));
          given()
              .contentType(ContentType.JSON)
              .header("Authorization", httpClient.authorizationHeader())
              .pathParam("vendorId", this.vendorOnboardingResponseDTO.getVendorId())
              .body(vendorUpdateDTO)
              .put(VENDORS_PATH + "/{vendorId}/status")
              .then()
              .assertThat()
              .statusCode(204);
        });

    When(
        "I try to modify vendor's active status to {string}",
        (String active) -> {
          var vendorUpdateDTO = new VendorStatusUpdateDTO(Boolean.parseBoolean(active));
          var targetVendorId = UUID.randomUUID();
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .header("Authorization", httpClient.authorizationHeader())
                  .pathParam("vendorId", targetVendorId)
                  .body(vendorUpdateDTO)
                  .put(VENDORS_PATH + "/{vendorId}/status");
          httpClient.recordResponse(response.statusCode(), response.asString());
        });

    Then(
        "the vendor active status is {word}",
        (String active) -> {
          var persistedStatus =
              jdbcTemplate.queryForObject(
                  "SELECT c_is_active FROM t_vendors WHERE c_id = ?",
                  Boolean.class,
                  this.vendorOnboardingResponseDTO.getVendorId());
          assertThat(persistedStatus).isEqualTo(Boolean.parseBoolean(active));
        });

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
                  this.vendorOnboardingResponseDTO.getVendorId());

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

    When(
        "I try to access my vendor session",
        () -> {
          var vendorToken =
              issuedSessionTokens.isEmpty()
                  ? Optional.ofNullable(this.vendorOnboardingResponseDTO)
                      .map(VendorOnboardingResponseDTO::getSessionToken)
                      .orElse(sessionToken)
                  : issuedSessionTokens.getFirst();
          var response =
              given().header("Authorization", "Bearer " + vendorToken).get("/vendor/session");
          httpClient.recordResponse(response.statusCode(), response.asString());
        });

    When(
        "I try to access vendor session with the revoked token",
        () -> {
          var response =
              given().header("Authorization", "Bearer " + sessionToken).get("/vendor/session");
          httpClient.recordResponse(response.statusCode(), response.asString());
        });

    When(
        "I try to list vendors with the vendor session token",
        () -> {
          var response =
              given().header("Authorization", "Bearer " + sessionToken).get("/admin/vendors");
          httpClient.recordResponse(response.statusCode(), response.asString());
        });
  }

  private String currentSessionToken() {
    if (!issuedSessionTokens.isEmpty()) {
      return issuedSessionTokens.getLast();
    }
    return vendorOnboardingResponseDTO.getSessionToken();
  }

  private void listVendors(Integer page, Integer size, String active, String name, String sort) {
    var request = given().header("Authorization", httpClient.authorizationHeader());
    request.queryParam("page", page).queryParam("size", size);
    if (!"any".equals(active)) {
      request.queryParam("isActive", active);
    }
    if (!"any".equals(name)) {
      request.queryParam("name", name);
    }
    if (sort != null && !"default".equals(sort)) {
      request.queryParam("sort", sort);
    }
    var response = request.get(VENDORS_PATH);
    httpClient.recordResponse(response.statusCode(), response.asString());
  }

  private static Map<String, Object> toCreateRequest(Map<String, String> values) {
    var request = commonRequest(values);
    request.put("addressText", values.get("addressText"));
    request.remove("isActive");
    return request;
  }

  private static Map<String, Object> toUpdateRequest(Map<String, String> values) {
    var request = commonRequest(values);
    request.remove("isActive");
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
    if (values.containsKey("deliveryFeeType")) {
      request.put("deliveryFeeType", blankToNull(values.get("deliveryFeeType")));
    }
    if (values.containsKey("deliveryFeeAmount")) {
      request.put("deliveryFeeAmount", parseInteger(values.get("deliveryFeeAmount")));
    }
    if (values.containsKey("isActive")) {
      request.put("isActive", parseBoolean(values.get("isActive")));
    }
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
