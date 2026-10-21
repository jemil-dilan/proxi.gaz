/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NonLocalSecretsValidatorTest {

  private static final String STRONG_SECRET =
      "staging-or-prod-secret-key-0123456789abcdef0123456789";

  @Test
  void localProfile_allowsDevPlaceholder() {
    assertThatCode(
            () ->
                NonLocalSecretsValidator.validate(
                    Set.of("local"), NonLocalSecretsValidator.LOCAL_DEV_JWT_PLACEHOLDER))
        .doesNotThrowAnyException();
  }

  @Test
  void noProfile_doesNotFail() {
    assertThatCode(() -> NonLocalSecretsValidator.validate(Set.of(), STRONG_SECRET))
        .doesNotThrowAnyException();
  }

  @ParameterizedTest
  @ValueSource(strings = {"staging", "production"})
  void nonLocal_rejectsMissingSecret(String profile) {
    assertThatThrownBy(() -> NonLocalSecretsValidator.validate(Set.of(profile), " "))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("JWT_SECRET");
  }

  @ParameterizedTest
  @ValueSource(strings = {"staging", "production"})
  void nonLocal_rejectsLocalDevPlaceholder(String profile) {
    assertThatThrownBy(
            () ->
                NonLocalSecretsValidator.validate(
                    Set.of(profile), NonLocalSecretsValidator.LOCAL_DEV_JWT_PLACEHOLDER))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("local development placeholder");
  }

  @ParameterizedTest
  @ValueSource(strings = {"staging", "production"})
  void nonLocal_rejectsShortSecret(String profile) {
    assertThatThrownBy(() -> NonLocalSecretsValidator.validate(Set.of(profile), "too-short"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("at least");
  }

  @ParameterizedTest
  @ValueSource(strings = {"staging", "production"})
  void nonLocal_acceptsStrongSecret(String profile) {
    assertThatCode(() -> NonLocalSecretsValidator.validate(Set.of(profile), STRONG_SECRET))
        .doesNotThrowAnyException();
  }
}
