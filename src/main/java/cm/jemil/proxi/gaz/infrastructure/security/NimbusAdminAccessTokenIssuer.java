/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.security;

import cm.jemil.proxi.gaz.application.outbound.AdminAccessTokenIssuer;
import cm.jemil.proxi.gaz.application.outbound.Clock;
import cm.jemil.proxi.gaz.config.Scopes;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
public class NimbusAdminAccessTokenIssuer implements AdminAccessTokenIssuer {

  private final JwtEncoder jwtEncoder;
  private final Clock clock;
  private final Duration tokenTtl;

  public NimbusAdminAccessTokenIssuer(
      JwtEncoder jwtEncoder,
      Clock clock,
      @Value("${proxigaz.security.jwt.access-token-ttl:15m}") Duration tokenTtl) {
    this.jwtEncoder = jwtEncoder;
    this.clock = clock;
    if (tokenTtl.isNegative() || tokenTtl.toSeconds() <= 0) {
      throw new IllegalArgumentException("JWT access-token lifetime must be at least one second");
    }
    this.tokenTtl = tokenTtl;
  }

  @Override
  public IssuedAccessToken issue(UserId adminUserId, Email email) {
    var issuedAt = clock.now();
    var expiresAt = issuedAt.plus(tokenTtl);
    var claims =
        JwtClaimsSet.builder()
            .subject(adminUserId.value().toString())
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .claim("email", email.value())
            .claim("scope", Scopes.ADMIN_SCOPE)
            .build();
    var header = JwsHeader.with(MacAlgorithm.HS256).build();
    var token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims));
    return new IssuedAccessToken(token.getTokenValue(), tokenTtl.toSeconds());
  }
}
