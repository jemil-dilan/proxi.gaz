/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.e2e.steps;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import cm.jemil.proxi.gaz.e2e.HttpClient;
import io.cucumber.datatable.DataTable;
import io.cucumber.java8.En;
import io.restassured.path.json.JsonPath;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

public class AuditLogSteps implements En {

  private static final String AUDIT_LOG_PATH = "/admin/audit-log";
  private static final JsonMapper JSON = JsonMapper.builder().build();

  private JsonPath auditPage;
  private String auditPageBody = "";

  public AuditLogSteps(HttpClient httpClient, JdbcTemplate jdbcTemplate) {
    var jdbcClient = JdbcClient.create(jdbcTemplate);

    When(
        "I list the audit log for that vendor",
        () -> {
          var response =
              given()
                  .header("Authorization", httpClient.authorizationHeader())
                  .queryParam("targetType", "VENDOR")
                  .queryParam("targetId", httpClient.getLastVendorId())
                  .get(AUDIT_LOG_PATH);
          httpClient.recordResponse(response.statusCode(), response.asString());
          assertThat(response.statusCode()).as(response.asString()).isEqualTo(200);
          auditPageBody = response.asString();
          auditPage = response.jsonPath();
        });

    When(
        "I try to list the audit log with the query {string}",
        (String query) -> {
          var request = given();
          if (httpClient.authorizationHeader() != null) {
            request = request.header("Authorization", httpClient.authorizationHeader());
          }
          var response =
              request.get(query.isBlank() ? AUDIT_LOG_PATH : AUDIT_LOG_PATH + "?" + query);
          httpClient.recordResponse(response.statusCode(), response.asString());
        });

    Then(
        "the audit log lists the following actions",
        (DataTable dataTable) ->
            assertThat(auditPage.getList("content.action", String.class))
                .containsExactlyElementsOf(dataTable.asList()));

    Then(
        "the audit log is empty",
        () -> {
          assertThat(auditPage.getList("content")).isEmpty();
          assertThat(auditPage.getLong("totalElements")).isZero();
        });

    Then(
        "every audit entry targets that vendor and was made by the admin {string}",
        (String adminEmail) -> {
          var adminId =
              jdbcClient
                  .sql("SELECT c_id FROM t_users WHERE c_email = ?")
                  .param(adminEmail)
                  .query(UUID.class)
                  .single();
          List<Map<String, Object>> entries = auditPage.getList("content");
          assertThat(entries).isNotEmpty();
          assertThat(entries)
              .allSatisfy(
                  entry -> {
                    assertThat(entry)
                        .containsEntry("targetType", "VENDOR")
                        .containsEntry("targetId", httpClient.getLastVendorId().toString())
                        .containsEntry("adminUserId", adminId.toString())
                        .containsEntry("adminEmail", adminEmail)
                        .containsKeys("id", "createdAt");
                  });
        });

    Then(
        "the audit entry {string} has the metadata {string}",
        (String action, String expectedJson) -> {
          List<Map<String, Object>> entries = auditPage.getList("content");
          var entry =
              entries.stream()
                  .filter(e -> action.equals(e.get("action")))
                  .findFirst()
                  .orElseThrow(() -> new AssertionError("No audit entry " + action));
          JsonNode actual = JSON.valueToTree(entry.get("metadata"));
          assertThat(actual).isEqualTo(JSON.readTree(expectedJson));
        });

    Then(
        "the audit log contains no issued vendor session token or token hash",
        () -> {
          assertThat(auditPage.getList("content")).isNotEmpty();
          assertThat(auditPageBody)
              .doesNotContainIgnoringCase("token")
              .doesNotContainIgnoringCase("password")
              .doesNotContainIgnoringCase("hash");
          for (var secret : secretsFor(httpClient, jdbcClient)) {
            assertThat(auditPageBody).doesNotContain(secret);
          }
        });

    Then(
        "the stored audit metadata for that vendor contains no issued vendor session token or token"
            + " hash",
        () -> {
          var stored =
              jdbcClient
                  .sql(
                      "SELECT c_metadata::text FROM t_admin_audit_log"
                          + " WHERE c_target_type = 'VENDOR' AND c_target_id = ?")
                  .param(httpClient.getLastVendorId())
                  .query(String.class)
                  .list();
          assertThat(stored).isNotEmpty();
          var secrets = secretsFor(httpClient, jdbcClient);
          assertThat(stored)
              .allSatisfy(
                  metadata -> {
                    assertThat(metadata).doesNotContainIgnoringCase("token");
                    secrets.forEach(secret -> assertThat(metadata).doesNotContain(secret));
                  });
        });
  }

  /** Raw tokens issued in the scenario, their SHA-256 hex, and every stored hash for the vendor. */
  private static List<String> secretsFor(HttpClient httpClient, JdbcClient jdbcClient) {
    var secrets = new ArrayList<String>();
    for (var token : httpClient.getIssuedVendorTokens()) {
      secrets.add(token);
      secrets.add(sha256(token));
    }
    secrets.addAll(
        jdbcClient
            .sql("SELECT c_token_hash FROM t_vendor_sessions WHERE c_vendor_id = ?")
            .param(httpClient.getLastVendorId())
            .query(String.class)
            .list());
    assertThat(secrets).isNotEmpty();
    return secrets;
  }

  private static String sha256(String value) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
