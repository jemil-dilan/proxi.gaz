/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import cm.jemil.proxi.gaz.infrastructure.time.FakeClock;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryTokenBucketRateLimiterTest {

  private FakeClock clock;
  private InMemoryTokenBucketRateLimiter rateLimiter;

  @BeforeEach
  void setUp() {
    clock = new FakeClock(Instant.EPOCH);
    rateLimiter = new InMemoryTokenBucketRateLimiter(clock);
  }

  @Test
  void allow_shouldReturnTrue_whenWithinLimit() {
    String key = "test-key";
    int maxAttempts = 5;
    int windowSeconds = 900;

    // First 5 attempts should be allowed
    for (int i = 0; i < 5; i++) {
      assertThat(rateLimiter.allow(key, maxAttempts, windowSeconds)).isTrue();
    }
  }

  @Test
  void allow_shouldReturnFalse_whenExceedingLimit() {
    String key = "test-key";
    int maxAttempts = 5;
    int windowSeconds = 900;

    // First 5 attempts should be allowed
    for (int i = 0; i < 5; i++) {
      rateLimiter.allow(key, maxAttempts, windowSeconds);
    }

    // 6th attempt should be rate limited
    assertThat(rateLimiter.allow(key, maxAttempts, windowSeconds)).isFalse();
  }

  @Test
  void allow_shouldResetAfterWindowExpires() {
    String key = "test-key";
    int maxAttempts = 5;
    int windowSeconds = 10; // 10 seconds for testing

    // Exhaust the limit
    for (int i = 0; i < 5; i++) {
      rateLimiter.allow(key, maxAttempts, windowSeconds);
    }

    // Should be rate limited
    assertThat(rateLimiter.allow(key, maxAttempts, windowSeconds)).isFalse();

    // Advance time past the window
    clock.setFixedInstant(Instant.EPOCH.plusSeconds(windowSeconds + 1));

    // Should be allowed again
    assertThat(rateLimiter.allow(key, maxAttempts, windowSeconds)).isTrue();
  }

  @Test
  void getRetryAfterSeconds_shouldReturnZero_whenNotRateLimited() {
    String key = "test-key";
    int maxAttempts = 5;
    int windowSeconds = 900;

    // Make one attempt
    rateLimiter.allow(key, maxAttempts, windowSeconds);

    assertThat(rateLimiter.getRetryAfterSeconds(key)).isZero();
  }

  @Test
  void getRetryAfterSeconds_shouldReturnRemainingTime_whenRateLimited() {
    String key = "test-key";
    int maxAttempts = 5;
    int windowSeconds = 10;

    // Exhaust the limit
    for (int i = 0; i < 5; i++) {
      rateLimiter.allow(key, maxAttempts, windowSeconds);
    }

    // Advance time by 5 seconds
    clock.setFixedInstant(Instant.EPOCH.plusSeconds(5));

    // Should return ~5 seconds remaining
    int retryAfter = rateLimiter.getRetryAfterSeconds(key);
    assertThat(retryAfter).isBetween(4, 6);
  }

  @Test
  void getRetryAfterSeconds_shouldReturnZero_whenWindowExpired() {
    String key = "test-key";
    int maxAttempts = 5;
    int windowSeconds = 10;

    // Exhaust the limit
    for (int i = 0; i < 5; i++) {
      rateLimiter.allow(key, maxAttempts, windowSeconds);
    }

    // Advance time past the window
    clock.setFixedInstant(Instant.EPOCH.plusSeconds(windowSeconds + 1));

    assertThat(rateLimiter.getRetryAfterSeconds(key)).isZero();
  }

  @Test
  void differentKeys_shouldHaveIndependentLimits() {
    int maxAttempts = 5;
    int windowSeconds = 900;

    // Exhaust limit for key1
    for (int i = 0; i < 5; i++) {
      rateLimiter.allow("key1", maxAttempts, windowSeconds);
    }

    // key1 should be rate limited
    assertThat(rateLimiter.allow("key1", maxAttempts, windowSeconds)).isFalse();

    // key2 should still be allowed
    assertThat(rateLimiter.allow("key2", maxAttempts, windowSeconds)).isTrue();
  }
}
