/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

/** API docs are public only where springdoc is enabled (local/staging), never by default. */
class ApiDocsExposureDataTest {

  private static final HttpClient HTTP =
      HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();

  static HttpResponse<String> anonymousGet(int port, String path) throws Exception {
    var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).build();
    return HTTP.send(request, HttpResponse.BodyHandlers.ofString());
  }

  @Nested
  @Tag("data")
  @ActiveProfiles("data_test")
  @SpringBootTest(
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      properties = {"springdoc.api-docs.enabled=true", "springdoc.swagger-ui.enabled=true"})
  class WhenEnabled {

    @LocalServerPort int port;

    @Test
    void apiDocs_shouldBePublic() throws Exception {
      var response = anonymousGet(port, "/v3/api-docs");

      assertThat(response.statusCode()).isEqualTo(200);
      assertThat(response.body()).contains("\"openapi\"");
    }

    @Test
    void swaggerUi_shouldBePublicIncludingItsAssets() throws Exception {
      assertThat(anonymousGet(port, "/swagger-ui.html").statusCode()).isEqualTo(200);
      assertThat(anonymousGet(port, "/swagger-ui/index.html").statusCode()).isEqualTo(200);
    }

    @Test
    void adminEndpoints_shouldStillRequireAuthentication() throws Exception {
      assertThat(anonymousGet(port, "/admin/vendors").statusCode()).isEqualTo(401);
    }
  }

  @Nested
  @Tag("data")
  @ActiveProfiles("data_test")
  @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
  class ByDefault {

    @LocalServerPort int port;

    @Test
    void apiDocs_shouldNotBeServed() throws Exception {
      assertThat(anonymousGet(port, "/v3/api-docs").statusCode()).isNotEqualTo(200);
    }

    @Test
    void swaggerUi_shouldNotBeServed() throws Exception {
      assertThat(anonymousGet(port, "/swagger-ui/index.html").statusCode()).isNotEqualTo(200);
    }

    @Test
    void health_shouldRemainPublic() throws Exception {
      assertThat(anonymousGet(port, "/actuator/health").statusCode()).isEqualTo(200);
    }
  }
}
