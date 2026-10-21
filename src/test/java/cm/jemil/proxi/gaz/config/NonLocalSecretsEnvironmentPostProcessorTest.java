/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

class NonLocalSecretsEnvironmentPostProcessorTest {

  private final NonLocalSecretsEnvironmentPostProcessor processor =
      new NonLocalSecretsEnvironmentPostProcessor();

  @Test
  void productionWithDevPlaceholder_fails() {
    MockEnvironment env = new MockEnvironment();
    env.setActiveProfiles("production");
    env.setProperty(
        "proxigaz.security.jwt.secret", NonLocalSecretsValidator.LOCAL_DEV_JWT_PLACEHOLDER);

    assertThatThrownBy(() -> processor.postProcessEnvironment(env, new SpringApplication()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("local development placeholder");
  }

  @Test
  void stagingWithStrongSecret_passes() {
    MockEnvironment env = new MockEnvironment();
    env.setActiveProfiles("staging");
    env.setProperty(
        "proxigaz.security.jwt.secret", "staging-or-prod-secret-key-0123456789abcdef0123456789");

    assertThatCode(() -> processor.postProcessEnvironment(env, new SpringApplication()))
        .doesNotThrowAnyException();
  }
}
