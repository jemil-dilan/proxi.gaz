package cm.jemil.proxi.gaz.e2e.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import cm.jemil.proxi.gaz.e2e.HttpClient;
import cm.jemil.proxi.gaz.e2e.HttpClient.LastRequestResponse;
import io.cucumber.java8.En;
import io.restassured.RestAssured;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

public class CommonSteps implements En {

  private final HttpClient httpClient;
  private final JwtDecoder jwtDecoder;
  private final JdbcClient jdbcClient;
  private final int port;

  public CommonSteps(
      HttpClient httpClient,
      JwtDecoder jwtDecoder,
      JdbcTemplate jdbcTemplate,
      Environment environment) {
    this.httpClient = httpClient;
    this.jwtDecoder = jwtDecoder;
    this.jdbcClient = JdbcClient.create(jdbcTemplate);
    this.port = environment.getRequiredProperty("local.server.port", Integer.class);

    Before(
        _ -> {
          RestAssured.baseURI = "http://localhost";
          RestAssured.port = port;
          when(jwtDecoder.decode(anyString())).thenAnswer(_ -> buildJwt());
          jdbcClient
              .sql("SELECT setval('seq_demo_reference', 100, true)")
              .query(Long.class)
              .single();
        });

    Given("I am connected with the scopes {string}",
        (String scopes) -> httpClient.setScopes(scopes));

    And(
        "The last request failed with the http status {string} and error code {string}",
        (String httpStatus, String errorCode) ->
            assertThat(httpClient.getLastResponse())
                .isNotNull()
                .isEqualTo(
                    new LastRequestResponse(
                        HttpStatus.valueOf(httpStatus.toUpperCase(Locale.ROOT)), errorCode)));
  }

  private Jwt buildJwt() {
    return Jwt.withTokenValue(HttpClient.ACCESS_TOKEN)
        .header("alg", "HS256")
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plus(5, ChronoUnit.MINUTES))
        .claim("scope", httpClient.getScopes())
        .build();
  }
}
