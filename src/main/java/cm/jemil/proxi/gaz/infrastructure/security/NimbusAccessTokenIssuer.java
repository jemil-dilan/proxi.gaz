/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.security;

import cm.jemil.proxi.gaz.application.outbound.AccessTokenIssuer;
import cm.jemil.proxi.gaz.application.outbound.Clock;
import cm.jemil.proxi.gaz.application.outbound.JwtSettings;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import java.util.Set;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

public class NimbusAccessTokenIssuer implements AccessTokenIssuer {

  private final JwtEncoder jwtEncoder;
  private final Clock clock;
  private final JwtSettings jwtSettings;

  public NimbusAccessTokenIssuer(JwtEncoder jwtEncoder, Clock clock, JwtSettings jwtSettings) {
    this.jwtEncoder = jwtEncoder;
    this.clock = clock;
    this.jwtSettings = jwtSettings;
    if (jwtSettings.accessTokenTtl().isNegative()
        || jwtSettings.accessTokenTtl().toSeconds() <= 0) {
      throw new IllegalArgumentException("JWT access-token lifetime must be at least one second");
    }
  }

  @Override
  public IssuedAccessToken issue(UserId userId, Email email, Set<String> scopes) {
    var issuedAt = clock.now();
    var tokenTtl = jwtSettings.accessTokenTtl();
    var expiresAt = issuedAt.plus(tokenTtl);
    var claims =
        JwtClaimsSet.builder()
            .subject(userId.value().toString())
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .claim("email", email.value())
            .claim("scope", String.join(" ", scopes))
            .build();
    var header = JwsHeader.with(MacAlgorithm.HS256).build();
    var token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims));
    return new IssuedAccessToken(token.getTokenValue(), tokenTtl.toSeconds());
  }
}
