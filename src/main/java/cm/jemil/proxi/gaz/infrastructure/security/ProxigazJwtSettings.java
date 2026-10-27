/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.security;

import cm.jemil.proxi.gaz.application.outbound.Configurations;
import cm.jemil.proxi.gaz.application.outbound.JwtSettings;
import java.time.Duration;
import lombok.RequiredArgsConstructor;

/** Adapts the bound application properties to the application JWT settings port. */
@RequiredArgsConstructor
public class ProxigazJwtSettings implements JwtSettings {

  private final Configurations configurations;

  @Override
  public String signingSecret() {
    return configurations.secret();
  }

  @Override
  public Duration accessTokenTtl() {
    return configurations.accessTokenTtl();
  }
}
