/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.e2e.steps;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import cm.jemil.proxi.gaz.e2e.HttpClient;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.AdminLoginDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.AdminProfileDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.LoginResponseDTO;
import io.cucumber.datatable.DataTable;
import io.cucumber.java8.En;
import io.restassured.http.ContentType;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AdminSteps implements En {

  private final JdbcClient jdbcClient;
  private LoginResponseDTO loginResponseDTO;
  private AdminProfileDTO adminProfileDTO;

  private static final String LOGIN_PATH = "/admin/auth/login";
  private static final String PROFILE_PATH = "/admin/auth/me";

  public AdminSteps(
      HttpClient httpClient, JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {

    this.jdbcClient = JdbcClient.create(jdbcTemplate);
    When(
        "I log in as admin with email {string} and password {string} from bootstrapped class",
        (String email, String password) -> {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .body(Map.of("email", email, "password", password))
                  .post(LOGIN_PATH);
          httpClient.recordResponse(response.statusCode(), response.asString());
          var accessToken = response.jsonPath().getString("accessToken");
          httpClient.setAccessToken(accessToken);
        });

    When(
        "I log in as admin with email {string} and password {string}",
        (String email, String password) -> {
          var adminLoginDTO = new AdminLoginDTO(email, password);
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .header("Authorization", httpClient.authorizationHeader())
                  .when()
                  .body(adminLoginDTO)
                  .post(LOGIN_PATH)
                  .then()
                  .statusCode(200)
                  .extract()
                  .response();
          this.loginResponseDTO = response.as(LoginResponseDTO.class);
          httpClient.setAccessToken(response.jsonPath().getString("accessToken"));
        });

    When(
        "I try to log in as admin with email {string} and password {string}",
        (String email, String password) -> {
          var adminLoginDTO = new AdminLoginDTO(email, password);
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .header("Authorization", httpClient.authorizationHeader())
                  .when()
                  .body(adminLoginDTO)
                  .post(LOGIN_PATH);
          httpClient.recordResponse(response.statusCode(), response.getBody().asString());
        });

    Then(
        "the login succeeds and returns an access token",
        () -> {
          assertThat(httpClient.getLastResponse().status().value()).isEqualTo(200);
          var body = io.restassured.path.json.JsonPath.from(httpClient.getLastResponseBody());
          assertThat((String) body.get("accessToken")).isNotBlank();
          assertThat((String) body.get("tokenType")).isEqualToIgnoringCase("Bearer");
          assertThat(((Number) body.get("expiresIn")).longValue()).isPositive();
        });

    Then(
        "the login succeeds and returns auth token",
        () -> {
          assertThat(this.loginResponseDTO.getAccessToken()).isNotBlank();
          assertThat(this.loginResponseDTO.getTokenType().getValue())
              .isEqualToIgnoringCase("Bearer");
          assertThat(this.loginResponseDTO.getExpiresIn().longValue()).isPositive();
        });

    When(
        "I access the protected admin profile",
        () -> {
          this.adminProfileDTO =
              given()
                  .contentType(ContentType.JSON)
                  .header("Authorization", httpClient.authorizationHeader())
                  .when()
                  .get(PROFILE_PATH)
                  .then()
                  .statusCode(200)
                  .extract()
                  .as(AdminProfileDTO.class);
        });

    Then(
        "the admin profile is returned for {string}",
        (String email) -> {
          assertThat(this.adminProfileDTO.getEmail()).isEqualTo(email);
        });

    And(
        "I assume the admin with the following data are in the database",
        (DataTable dataTable) -> {
          List<AdminData> rowsUnderTest =
              jdbcClient
                  .sql(
                      "SELECT u.c_id, u.c_email, u.c_password_hash, u.c_created_at "
                          + "FROM t_users u JOIN t_user_roles r ON r.c_user_id = u.c_id "
                          + "WHERE r.c_role = 'ADMIN'")
                  .query(
                      (rs, _) ->
                          new AdminData(
                              UUID.fromString(rs.getString("c_id")),
                              rs.getString("c_email"),
                              rs.getString("c_password_hash"),
                              Optional.ofNullable(rs.getTimestamp("c_created_at"))
                                  .map(Timestamp::toLocalDateTime)
                                  .orElse(null)))
                  .list();

          List<AdminData> expected =
              dataTable.asMaps().stream()
                  .map(
                      data ->
                          new AdminData(
                              Optional.ofNullable(data.get("id"))
                                  .map(UUID::fromString)
                                  .orElse(null),
                              data.get("email"),
                              data.get("password"),
                              Optional.ofNullable(data.get("created_at"))
                                  .map(LocalDateTime::parse)
                                  .orElse(null)))
                  .toList();

          for (int i = 0; i < expected.size(); i++) {
            var expectedAdmin = expected.get(i);
            var actualAdmin = rowsUnderTest.get(i);
            assertThat(actualAdmin.id()).isEqualTo(expectedAdmin.id());
            assertThat(actualAdmin.email()).isEqualTo(expectedAdmin.email());
            assertThat(passwordEncoder.matches(expectedAdmin.password(), actualAdmin.password()))
                .isTrue();
            assertThat(actualAdmin.createdAt()).isEqualTo(expectedAdmin.createdAt());
          }
        });

    When(
        "I try to access the protected admin profile with an invalid token",
        () -> {
          httpClient.setAccessToken("not-a-valid-jwt");
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .header("Authorization", httpClient.authorizationHeader())
                  .when()
                  .get(PROFILE_PATH);
          httpClient.recordResponse(response.statusCode(), response.getBody().asString());
        });

    When(
        "I try to access the protected admin profile",
        () -> {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .header("Authorization", httpClient.authorizationHeader())
                  .when()
                  .get(PROFILE_PATH);
          httpClient.recordResponse(response.statusCode(), response.getBody().asString());
        });
  }

  private record AdminData(UUID id, String email, String password, LocalDateTime createdAt) {}
}
