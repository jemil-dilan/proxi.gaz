/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.config;

import cm.jemil.proxi.gaz.application.outbound.Configurations;
import java.time.Duration;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConfigurationsImpl implements Configurations {
  private final Duration accessTokenTtl;
  private final String secret;
  private final boolean bootstrapEnabled;
  private final String bootstrapEmail;
  private final String bootstrapPassword;

  @Override
  public Duration accessTokenTtl() {
    return accessTokenTtl;
  }

  @Override
  public String secret() {
    return secret;
  }

  @Override
  public boolean bootstrapEnabled() {
    return bootstrapEnabled;
  }

  @Override
  public String bootstrapEmail() {
    return bootstrapEmail;
  }

  @Override
  public String bootstrapPassword() {
    return bootstrapPassword;
  }
}
