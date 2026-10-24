/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.e2e.steps;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import cm.jemil.proxi.gaz.e2e.HttpClient;
import io.cucumber.java8.En;
import io.restassured.http.ContentType;
import java.util.Map;

public class AdminAuthSteps implements En {

  private static final String LOGIN_PATH = "/admin/auth/login";
  private static final String PROFILE_PATH = "/admin/auth/me";

  public AdminAuthSteps(HttpClient httpClient) {
    When(
        "I log in as admin with email {string} and password {string}",
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

    Then(
        "the login succeeds and returns an access token",
        () -> {
          assertThat(httpClient.getLastResponse().status().value()).isEqualTo(200);
          var body = io.restassured.path.json.JsonPath.from(httpClient.getLastResponseBody());
          assertThat((String) body.get("accessToken")).isNotBlank();
          assertThat((String) body.get("tokenType")).isEqualToIgnoringCase("Bearer");
          assertThat(((Number) body.get("expiresIn")).longValue()).isPositive();
        });

    When(
        "I access the protected admin profile",
        () -> {
          var response =
              given().header("Authorization", httpClient.authorizationHeader()).get(PROFILE_PATH);
          httpClient.recordResponse(response.statusCode(), response.asString());
        });

    Then(
        "the admin profile is returned for {string}",
        (String email) -> {
          assertThat(httpClient.getLastResponse().status().value()).isEqualTo(200);
          var body = io.restassured.path.json.JsonPath.from(httpClient.getLastResponseBody());
          assertThat((String) body.get("email")).isEqualTo(email);
        });

    Then(
        "the login is rejected with a generic unauthorized response",
        () -> {
          assertThat(httpClient.getLastResponse().status().value()).isEqualTo(401);
          var body = io.restassured.path.json.JsonPath.from(httpClient.getLastResponseBody());
          assertThat((String) body.get("errorCode")).isEqualTo("PROXIGAZ-401-000");
          assertThat((String) body.get("message")).containsIgnoringCase("invalid credentials");
          assertThat(httpClient.getLastResponseBody())
              .doesNotContain("pxg010-admin@example.test", "PXG010-test-password");
        });

    When(
        "I access the protected admin profile without a token",
        () -> {
          httpClient.clearAccessToken();
          var response = given().get(PROFILE_PATH);
          httpClient.recordResponse(response.statusCode(), response.asString());
        });

    When(
        "I access the protected admin profile with an invalid token",
        () -> {
          httpClient.setAccessToken("not-a-valid-jwt");
          var response =
              given().header("Authorization", httpClient.authorizationHeader()).get(PROFILE_PATH);
          httpClient.recordResponse(response.statusCode(), response.asString());
        });
  }
}
