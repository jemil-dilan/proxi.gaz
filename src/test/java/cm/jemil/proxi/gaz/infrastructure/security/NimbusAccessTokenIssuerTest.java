/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import cm.jemil.proxi.gaz.application.outbound.AccessTokenIssuer.IssuedAccessToken;
import cm.jemil.proxi.gaz.application.outbound.JwtSettings;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import cm.jemil.proxi.gaz.infrastructure.time.FakeClock;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

@ExtendWith(MockitoExtension.class)
class NimbusAccessTokenIssuerTest {

  @Mock private JwtEncoder jwtEncoder;
  @Mock private JwtSettings jwtSettings;

  @Test
  void issue_shouldSetAdminSubjectEmailScopeAndExpiration() {
    var issuedAt = Instant.parse("2026-10-24T10:00:00Z");
    var adminId = UserId.from(UUID.randomUUID());
    var email = Email.from("admin@example.test");
    var jwt =
        Jwt.withTokenValue("signed-token")
            .header("alg", "HS256")
            .issuedAt(issuedAt)
            .expiresAt(issuedAt.plus(Duration.ofMinutes(15)))
            .build();
    given(jwtEncoder.encode(any(JwtEncoderParameters.class))).willReturn(jwt);
    given(jwtSettings.accessTokenTtl()).willReturn(Duration.ofMinutes(15));

    var tokenIssuer = new NimbusAccessTokenIssuer(jwtEncoder, new FakeClock(issuedAt), jwtSettings);

    IssuedAccessToken issuedToken = tokenIssuer.issue(adminId, email, Set.of("vendor:read"));

    var parametersCaptor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
    then(jwtEncoder).should().encode(parametersCaptor.capture());
    var claims = parametersCaptor.getValue().getClaims();
    assertThat(claims.getSubject()).isEqualTo(adminId.value().toString());
    assertThat(claims.getClaimAsString("email")).isEqualTo(email.value());
    assertThat(claims.getClaimAsString("scope")).isEqualTo("vendor:read");
    assertThat(claims.getExpiresAt()).isEqualTo(issuedAt.plus(Duration.ofMinutes(15)));
    assertThat(issuedToken.value()).isEqualTo("signed-token");
    assertThat(issuedToken.expiresInSeconds()).isEqualTo(900);
  }
}
