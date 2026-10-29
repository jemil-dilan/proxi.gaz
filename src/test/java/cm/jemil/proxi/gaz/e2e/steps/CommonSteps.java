/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.e2e.steps;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

public class CommonSteps implements En {

  private final HttpClient httpClient;
  private final JwtEncoder jwtEncoder;
  private final JdbcClient jdbcClient;
  private final int port;

  public CommonSteps(
      HttpClient httpClient,
      JwtEncoder jwtEncoder,
      JdbcTemplate jdbcTemplate,
      Environment environment) {
    this.httpClient = httpClient;
    this.jwtEncoder = jwtEncoder;
    this.jdbcClient = JdbcClient.create(jdbcTemplate);
    this.port = environment.getRequiredProperty("local.server.port", Integer.class);

    Before(
        _ -> {
          RestAssured.baseURI = "http://localhost";
          RestAssured.port = port;
          httpClient.setScopes("");
          httpClient.clearAccessToken();
          jdbcClient
              .sql("SELECT setval('seq_demo_reference', 100, true)")
              .query(Long.class)
              .single();
        });

    Given(
        "I am connected with the scopes {string}",
        (String scopes) -> {
          httpClient.setScopes(scopes);
          httpClient.setAccessToken(buildAccessToken(scopes));
        });

    Given(
        "I am connected with an admin token with subject {string}",
        (String subject) -> {
          var now = Instant.now();
          var claims =
              JwtClaimsSet.builder()
                  .subject(subject)
                  .issuedAt(now)
                  .expiresAt(now.plus(5, ChronoUnit.MINUTES))
                  .claim("scope", "admin")
                  .build();
          var header = JwsHeader.with(MacAlgorithm.HS256).build();
          httpClient.setAccessToken(
              jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue());
        });

    And(
        "The last request failed with the http status {string} and error code {string}",
        (String httpStatus, String errorCode) ->
            assertThat(httpClient.getLastResponse())
                .isNotNull()
                .isEqualTo(
                    new LastRequestResponse(
                        HttpStatus.valueOf(httpStatus.toUpperCase(Locale.ROOT)), errorCode)));
  }

  private String buildAccessToken(String scopes) {
    var now = Instant.now();
    var claims =
        JwtClaimsSet.builder()
            .subject("proxigaz-e2e-user")
            .issuedAt(now)
            .expiresAt(now.plus(5, ChronoUnit.MINUTES))
            .claim("scope", scopes)
            .build();
    var header = JwsHeader.with(MacAlgorithm.HS256).build();
    return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
  }
}
