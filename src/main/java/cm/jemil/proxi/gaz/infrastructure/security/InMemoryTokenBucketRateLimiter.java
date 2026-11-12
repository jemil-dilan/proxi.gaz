/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.security;

import cm.jemil.proxi.gaz.application.outbound.Clock;
import cm.jemil.proxi.gaz.application.outbound.RateLimiter;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;

/**
 * In-memory token bucket rate limiter implementation (D17). Uses a simple token bucket algorithm
 * with fixed window.
 */
@RequiredArgsConstructor
public class InMemoryTokenBucketRateLimiter implements RateLimiter {

  private final Clock clock;

  // Map: key -> TokenBucket
  private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

  @Override
  public boolean allow(String key, int maxAttempts, int windowSeconds) {
    var bucket = buckets.computeIfAbsent(key, _ -> new TokenBucket(maxAttempts, windowSeconds));
    return bucket.allow(clock.now());
  }

  @Override
  public int getRetryAfterSeconds(String key) {
    var bucket = buckets.get(key);
    if (bucket == null) {
      return 0;
    }
    return bucket.getRetryAfterSeconds(clock.now());
  }

  /** Clears all buckets. Intended for test isolation (e.g. Cucumber scenarios). */
  public void clear() {
    buckets.clear();
  }

  /** Token bucket for rate limiting. Tracks attempts within a sliding window. */
  private static class TokenBucket {
    private final int maxAttempts;
    private final int windowSeconds;
    private int attempts;
    private long windowStartMs;

    TokenBucket(int maxAttempts, int windowSeconds) {
      this.maxAttempts = maxAttempts;
      this.windowSeconds = windowSeconds;
      this.attempts = 0;
      this.windowStartMs = Long.MIN_VALUE; // Sentinel value for "not initialized"
    }

    synchronized boolean allow(Instant now) {
      long currentMs = now.toEpochMilli();

      // Reset if window has expired or not yet initialized
      if (windowStartMs == Long.MIN_VALUE || currentMs - windowStartMs > windowSeconds * 1000L) {
        attempts = 1;
        windowStartMs = currentMs;
        return true;
      }

      // Within current window
      if (attempts < maxAttempts) {
        attempts++;
        return true;
      }

      return false;
    }

    synchronized int getRetryAfterSeconds(Instant now) {
      if (attempts < maxAttempts) {
        return 0;
      }
      long currentMs = now.toEpochMilli();
      long elapsed = currentMs - windowStartMs;
      long remaining = windowSeconds * 1000L - elapsed;
      // If window has expired, no retry needed (limit has reset)
      if (remaining <= 0) {
        return 0;
      }
      return (int) Math.ceil(remaining / 1000.0);
    }
  }
}
