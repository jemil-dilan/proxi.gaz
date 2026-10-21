/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "proxigaz")
public record ProxigazProperties(Security security) {

  public record Security(Jwt jwt) {

    public record Jwt(String secret) {}
  }
}
