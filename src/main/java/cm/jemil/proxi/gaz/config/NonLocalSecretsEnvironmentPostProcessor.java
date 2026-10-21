/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.config;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/** Fail-fast when a non-local profile is active without a suitable JWT secret. */
public class NonLocalSecretsEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

  @Override
  public void postProcessEnvironment(
      ConfigurableEnvironment environment, SpringApplication application) {
    NonLocalSecretsValidator.validate(
        Arrays.stream(environment.getActiveProfiles()).collect(Collectors.toSet()),
        environment.getProperty("proxigaz.security.jwt.secret"));
  }

  @Override
  public int getOrder() {
    // After config data (application-*.yaml) is loaded
    return Ordered.LOWEST_PRECEDENCE - 10;
  }
}
