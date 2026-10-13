/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.e2e.steps;

import static cm.jemil.proxi.gaz.e2e.HttpClient.ACCESS_TOKEN_WITH_BEARER;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import cm.jemil.proxi.gaz.domain.Constants;
import cm.jemil.proxi.gaz.e2e.HttpClient;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.CreationResponseDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.DemoCreateDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.DemoDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.DemoUpdateDTO;
import io.cucumber.datatable.DataTable;
import io.cucumber.java8.En;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.assertj.core.groups.Tuple;
import org.awaitility.Awaitility;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;

public class DemoSteps implements En {

  private final HttpClient httpClient;
  private final JdbcClient jdbcClient;

  private CreationResponseDTO creationResponseDTO;
  private List<DemoDTO> demos;
  private DemoDTO demo;

  public DemoSteps(HttpClient httpClient, JdbcTemplate jdbcTemplate) {
    this.httpClient = httpClient;
    this.jdbcClient = JdbcClient.create(jdbcTemplate);

    And(
        "I assume that the demos with the following data are inside the database",
        (DataTable dataTable) -> {
          List<DemoData> rowsUnderTest =
              jdbcClient
                  .sql("SELECT * FROM t_demo")
                  .query(
                      (rs, _) ->
                          new DemoData(
                              UUID.fromString(rs.getString("c_id")),
                              rs.getString("c_name"),
                              rs.getString("c_reference"),
                              rs.getString("c_description"),
                              rs.getBoolean("c_deleted")))
                  .list();

          List<DemoData> expected =
              dataTable.asMaps().stream()
                  .map(
                      data ->
                          new DemoData(
                              UUID.fromString(data.get("id")),
                              data.get("name"),
                              data.get("reference"),
                              data.get("description"),
                              Boolean.parseBoolean(data.get("deleted"))))
                  .toList();

          assertThat(rowsUnderTest).containsAll(expected);
        });

    And(
        "I assume that the demos with the following data are not inside the database",
        (DataTable dataTable) -> {
          List<String> idsInsideDatabase =
              jdbcClient.sql("SELECT c_id FROM t_demo").query(String.class).list();
          List<String> expectedAbsent =
              dataTable.asMaps().stream().map(data -> data.get("id")).toList();
          assertThat(idsInsideDatabase).doesNotContainAnyElementsOf(expectedAbsent);
        });

    And(
        "I assume that no demo with the following name is inside the database",
        (DataTable dataTable) -> {
          for (Map<String, String> row : dataTable.asMaps()) {
            Integer count =
                jdbcClient
                    .sql("SELECT COUNT(*) FROM t_demo WHERE c_name = ?")
                    .param(row.get("name"))
                    .query(Integer.class)
                    .single();
            assertThat(count).isNotNull().isZero();
          }
        });

    And(
        "I fetch all demos using the extraction code {string}",
        (String fieldsExtractionCode) ->
            this.demos =
                given()
                    .header("Authorization", ACCESS_TOKEN_WITH_BEARER)
                    .queryParam(Constants.FIELDS_TO_EXTRACT_CODE, fieldsExtractionCode)
                    .when()
                    .get("/demo")
                    .then()
                    .statusCode(200)
                    .extract()
                    .as(new TypeRef<List<DemoDTO>>() {}));

    And(
        "I try to fetch all demos using the extraction code {string}",
        (String fieldsExtractionCode) -> {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .header("Authorization", ACCESS_TOKEN_WITH_BEARER)
                  .queryParam(Constants.FIELDS_TO_EXTRACT_CODE, fieldsExtractionCode)
                  .when()
                  .get("/demo");
          httpClient.recordResponse(response.statusCode(), response.getBody().asString());
        });

    And(
        "I should see that the following demos are among the fetched ones",
        (DataTable dataTable) ->
            assertThat(demos)
                .containsAll(
                    dataTable.asMaps().stream()
                        .map(
                            data ->
                                new DemoDTO()
                                    .id(UUID.fromString(data.get("id")))
                                    .reference(data.get("reference"))
                                    .name(data.get("name")))
                        .toList()));

    And(
        "I should see that the following data are not among the fetched ones",
        (DataTable dataTable) ->
            assertThat(demos)
                .doesNotContainAnyElementsOf(
                    dataTable.asMaps().stream()
                        .map(
                            data ->
                                new DemoDTO()
                                    .id(UUID.fromString(data.get("id")))
                                    .reference(data.get("reference"))
                                    .name(data.get("name")))
                        .toList()));

    And(
        "I create the Demo with the following data",
        (DataTable dataTable) -> {
          var entry = dataTable.asMaps().getFirst();
          var demoCreateDTO =
              new DemoCreateDTO().name(entry.get("name")).description(entry.get("description"));
          this.creationResponseDTO =
              given()
                  .contentType(ContentType.JSON)
                  .header("Authorization", ACCESS_TOKEN_WITH_BEARER)
                  .when()
                  .body(demoCreateDTO)
                  .post("/demo")
                  .then()
                  .statusCode(201)
                  .extract()
                  .as(CreationResponseDTO.class);
          this.httpClient.lastUpdatedId = creationResponseDTO.getNewId();
        });

    When(
        "I try to create a new Demo with the following data",
        (DataTable dataTable) -> {
          var row = dataTable.asMaps().getFirst();
          var demoCreateDTO =
              new DemoCreateDTO().name(row.get("name")).description(row.get("description"));
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .header("Authorization", ACCESS_TOKEN_WITH_BEARER)
                  .when()
                  .body(demoCreateDTO)
                  .post("/demo");
          httpClient.recordResponse(response.statusCode(), response.getBody().asString());
        });

    And(
        "I try to create a new Demo named {string}",
        (String name) -> {
          var demoCreateDTO = new DemoCreateDTO().name(name);
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .header("Authorization", ACCESS_TOKEN_WITH_BEARER)
                  .when()
                  .body(demoCreateDTO)
                  .post("/demo");
          httpClient.recordResponse(response.statusCode(), response.getBody().asString());
        });

    And(
        "After max {int} seconds, I should see that the demo with the following data is persisted"
            + " in the database",
        (Integer maxWaitingTimeInSeconds, DataTable dataTable) -> {
          UUID demoId = httpClient.lastUpdatedId;
          Map<String, String> expected = dataTable.asMaps().getFirst();

          Tuple expectedTuple =
              new Tuple(
                  expected.get("name"),
                  expected.get("reference"),
                  expected.get("description"),
                  Boolean.parseBoolean(expected.get("deleted")));

          Awaitility.await()
              .atMost(maxWaitingTimeInSeconds, TimeUnit.SECONDS)
              .untilAsserted(
                  () -> {
                    List<Tuple> actualTuples =
                        jdbcClient
                            .sql(
                                """
                                SELECT c_name, c_reference, c_description, c_deleted
                                FROM t_demo
                                WHERE c_id = ?
                                """)
                            .params(demoId)
                            .query(
                                (rs, _) ->
                                    new Tuple(
                                        rs.getString("c_name"),
                                        rs.getString("c_reference"),
                                        rs.getString("c_description"),
                                        rs.getBoolean("c_deleted")))
                            .list();
                    assertThat(actualTuples).contains(expectedTuple);
                  });
        });

    And(
        "I should see that the Demo's immutable fields have not been modified",
        (DataTable dataTable) -> {
          UUID demoId = httpClient.lastUpdatedId;
          Map<String, String> expected = dataTable.asMaps().getFirst();

          Awaitility.await()
              .atMost(5, TimeUnit.SECONDS)
              .untilAsserted(
                  () -> {
                    List<Tuple> actualTuples =
                        jdbcClient
                            .sql(
                                """
                                SELECT c_id, c_reference
                                FROM t_demo
                                WHERE c_id = ?
                                """)
                            .params(demoId)
                            .query(
                                (rs, _) ->
                                    new Tuple(
                                        UUID.fromString(rs.getString("c_id")),
                                        rs.getString("c_reference")))
                            .list();
                    assertThat(actualTuples)
                        .containsExactly(
                            new Tuple(
                                UUID.fromString(expected.get("id")), expected.get("reference")));
                  });
        });

    And(
        "I fetch demo identify by {string} using the extraction code {string}",
        (String demoId, String fieldsExtractionCode) ->
            this.demo =
                given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", ACCESS_TOKEN_WITH_BEARER)
                    .pathParam("demoId", UUID.fromString(demoId))
                    .queryParam(Constants.FIELDS_TO_EXTRACT_CODE, fieldsExtractionCode)
                    .when()
                    .get("/demo/{demoId}")
                    .then()
                    .statusCode(200)
                    .extract()
                    .as(DemoDTO.class));

    And(
        "I try to fetch demo identify by {string} using the extraction code {string}",
        (String demoId, String fieldsExtractionCode) -> {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .header("Authorization", ACCESS_TOKEN_WITH_BEARER)
                  .pathParam("demoId", UUID.fromString(demoId))
                  .queryParam(Constants.FIELDS_TO_EXTRACT_CODE, fieldsExtractionCode)
                  .when()
                  .get("/demo/{demoId}");
          httpClient.recordResponse(response.statusCode(), response.getBody().asString());
        });

    And(
        "I should see that demo with the following data have been fetched",
        (DataTable dataTable) -> {
          final var map = dataTable.asMaps().getFirst();
          assertThat(demo)
              .returns(UUID.fromString(map.get("id")), DemoDTO::getId)
              .returns(map.get("reference"), DemoDTO::getReference)
              .returns(map.get("description"), DemoDTO::getDescription)
              .returns(map.get("name"), DemoDTO::getName);
        });

    When(
        "I update the Demo identify by {string} with the following data",
        (String demoId, DataTable dataTable) -> {
          final var row = dataTable.asMaps().getFirst();
          DemoUpdateDTO updateDemoDTO = toUpdateDemoDTO(row);
          httpClient.lastUpdatedId = UUID.fromString(demoId);
          given()
              .contentType(ContentType.JSON)
              .header("Authorization", ACCESS_TOKEN_WITH_BEARER)
              .pathParam("demoId", httpClient.lastUpdatedId)
              .when()
              .body(updateDemoDTO)
              .put("/demo/{demoId}")
              .then()
              .statusCode(204);
        });

    When(
        "I try to update the Demo identify by {string} with the following data",
        (String demoId, DataTable dataTable) -> {
          final var row = dataTable.asMaps().getFirst();
          DemoUpdateDTO updateDemoDTO = toUpdateDemoDTO(row);
          httpClient.lastUpdatedId = UUID.fromString(demoId);
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .header("Authorization", ACCESS_TOKEN_WITH_BEARER)
                  .pathParam("demoId", httpClient.lastUpdatedId)
                  .when()
                  .body(updateDemoDTO)
                  .put("/demo/{demoId}");
          httpClient.recordResponse(response.statusCode(), response.getBody().asString());
        });

    And(
        "I delete the Demo identify by {string}",
        (String demoId) -> {
          httpClient.lastUpdatedId = UUID.fromString(demoId);
          given()
              .contentType(ContentType.JSON)
              .header("Authorization", ACCESS_TOKEN_WITH_BEARER)
              .pathParam("demoId", httpClient.lastUpdatedId)
              .when()
              .delete("/demo/{demoId}")
              .then()
              .statusCode(204);
        });

    And(
        "I try to delete the Demo identify by {string}",
        (String demoId) -> {
          httpClient.lastUpdatedId = UUID.fromString(demoId);
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .header("Authorization", ACCESS_TOKEN_WITH_BEARER)
                  .pathParam("demoId", httpClient.lastUpdatedId)
                  .when()
                  .delete("/demo/{demoId}");
          httpClient.recordResponse(response.statusCode(), response.getBody().asString());
        });
  }

  private static DemoUpdateDTO toUpdateDemoDTO(Map<String, String> row) {
    var dto = new DemoUpdateDTO().name(row.get("name"));
    if (row.get("description") != null) {
      dto = dto.description(row.get("description"));
    }
    return dto;
  }

  private record DemoData(
      UUID id, String name, String reference, String description, boolean deleted) {}
}
