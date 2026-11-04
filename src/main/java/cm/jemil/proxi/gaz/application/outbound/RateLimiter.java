/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.outbound;

/**
 * Port for rate limiting operations.
 * Implementation: in-memory token bucket (D17).
 */
public interface RateLimiter {

  /**
   * Check if the operation is allowed for the given key.
   *
   * @param key the rate limit key (e.g., email+IP for login attempts)
   * @param maxAttempts maximum number of attempts allowed
   * @param windowSeconds time window in seconds
   * @return true if allowed, false if rate limited
   */
  boolean allow(String key, int maxAttempts, int windowSeconds);

  /**
   * Get the remaining time in seconds until the rate limit resets for the given key.
   *
   * @param key the rate limit key
   * @return remaining seconds, or 0 if not rate limited
   */
  int getRetryAfterSeconds(String key);
}
