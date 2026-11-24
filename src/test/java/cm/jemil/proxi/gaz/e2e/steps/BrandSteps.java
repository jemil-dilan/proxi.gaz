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
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;

public class BrandSteps implements En {

  private static final String BRANDS_PATH = "/admin/brands";

  private JsonPath createdBrand;
  private List<String> listedNames = List.of();
  private JsonPath brandAudit;

  public BrandSteps(HttpClient httpClient, JdbcTemplate jdbcTemplate) {
    var jdbcClient = JdbcClient.create(jdbcTemplate);

    When(
        "I create the brand {string}",
        (String name) -> {
          var response = authorized(httpClient).body(Map.of("name", name)).post(BRANDS_PATH);
          httpClient.recordResponse(response.statusCode(), response.asString());
          assertThat(response.statusCode()).as(response.asString()).isEqualTo(201);
          createdBrand = response.jsonPath();
        });

    When(
        "I try to create the brand {string}",
        (String name) -> {
          var response = authorized(httpClient).body(Map.of("name", name)).post(BRANDS_PATH);
          httpClient.recordResponse(response.statusCode(), response.asString());
        });

    Then(
        "the created brand is named {string} and is active",
        (String name) -> {
          assertThat(createdBrand.getString("name")).isEqualTo(name);
          assertThat(createdBrand.getBoolean("isActive")).isTrue();
          assertThat(UUID.fromString(createdBrand.getString("id"))).isNotNull();
          assertThat(createdBrand.getString("createdAt")).isNotBlank();
        });

    When(
        "I list the brands with isActive {string}",
        (String isActive) -> {
          var response = authorized(httpClient).queryParam("isActive", isActive).get(BRANDS_PATH);
          httpClient.recordResponse(response.statusCode(), response.asString());
          assertThat(response.statusCode()).as(response.asString()).isEqualTo(200);
          listedNames = response.jsonPath().getList("name", String.class);
        });

    When(
        "I try to list the brands",
        () -> {
          var response = authorized(httpClient).get(BRANDS_PATH);
          httpClient.recordResponse(response.statusCode(), response.asString());
        });

    Then(
        "the brand list contains {string}",
        (String name) -> assertThat(listedNames).contains(name));

    Then(
        "the brand list does not contain {string}",
        (String name) -> assertThat(listedNames).doesNotContain(name));

    Then(
        "the brand list is sorted by name",
        () ->
            assertThat(listedNames)
                .isSortedAccordingTo(Comparator.comparing(name -> name.toLowerCase())));

    When(
        "I list the audit log for that brand",
        () -> {
          var response =
              authorized(httpClient)
                  .queryParam("targetType", "BRAND")
                  .queryParam("targetId", createdBrand.getString("id"))
                  .get("/admin/audit-log");
          assertThat(response.statusCode()).as(response.asString()).isEqualTo(200);
          brandAudit = response.jsonPath();
        });

    Then(
        "the brand audit log has one {string} entry by the admin {string}",
        (String action, String adminEmail) -> {
          var adminId =
              jdbcClient
                  .sql("SELECT c_id FROM t_users WHERE c_email = ?")
                  .param(adminEmail)
                  .query(UUID.class)
                  .single();
          List<Map<String, Object>> entries = brandAudit.getList("content");
          assertThat(entries)
              .singleElement()
              .satisfies(
                  entry ->
                      assertThat(entry)
                          .containsEntry("action", action)
                          .containsEntry("targetType", "BRAND")
                          .containsEntry("targetId", createdBrand.getString("id"))
                          .containsEntry("adminUserId", adminId.toString()));
        });
  }

  private static RequestSpecification authorized(HttpClient httpClient) {
    var request = given().contentType(ContentType.JSON);
    var authorization = httpClient.authorizationHeader();
    return authorization == null ? request : request.header("Authorization", authorization);
  }
}
