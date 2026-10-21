/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.config;

import java.util.Locale;
import java.util.Set;

/** Ensures staging/production never boot with missing, short, or local-dev JWT secrets. */
public final class NonLocalSecretsValidator {

  /** Must match the default in {@code application-local.yaml}. */
  public static final String LOCAL_DEV_JWT_PLACEHOLDER =
      "proxigaz-dev-secret-key-0123456789abcdef0123456789abcdef";

  private static final int MIN_SECRET_LENGTH = 32;
  private static final Set<String> NON_LOCAL_PROFILES = Set.of("staging", "production");

  private NonLocalSecretsValidator() {}

  public static void validate(Set<String> activeProfiles, String jwtSecret) {
    boolean nonLocal =
        activeProfiles.stream()
            .map(p -> p.toLowerCase(Locale.ROOT))
            .anyMatch(NON_LOCAL_PROFILES::contains);
    if (!nonLocal) {
      return;
    }

    if (jwtSecret == null || jwtSecret.isBlank()) {
      throw new IllegalStateException(
          "JWT_SECRET (proxigaz.security.jwt.secret) must be set for staging/production profiles");
    }
    if (LOCAL_DEV_JWT_PLACEHOLDER.equals(jwtSecret)) {
      throw new IllegalStateException(
          "JWT_SECRET must not use the local development placeholder in staging/production");
    }
    if (jwtSecret.length() < MIN_SECRET_LENGTH) {
      throw new IllegalStateException(
          "JWT_SECRET must be at least "
              + MIN_SECRET_LENGTH
              + " characters for staging/production");
    }
  }
}
