/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.security;

import cm.jemil.proxi.gaz.application.outbound.RateLimiter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fake rate limiter for testing with FakeClock. Allows manual control over rate limiting behavior.
 */
public class FakeRateLimiter implements RateLimiter {

  private final Map<String, Integer> attempts = new ConcurrentHashMap<>();
  private final Map<String, Integer> maxAttemptsMap = new ConcurrentHashMap<>();
  private final Map<String, Long> windowStart = new ConcurrentHashMap<>();
  private final Map<String, Integer> windowSecondsMap = new ConcurrentHashMap<>();

  public void setLimit(String key, int maxAttempts, int windowSeconds) {
    maxAttemptsMap.put(key, maxAttempts);
    windowSecondsMap.put(key, windowSeconds);
  }

  public void reset(String key) {
    attempts.remove(key);
    windowStart.remove(key);
  }

  public void clear() {
    attempts.clear();
    windowStart.clear();
  }

  @Override
  public boolean allow(String key, int maxAttempts, int windowSeconds) {
    // Store the limit for this key
    if (!maxAttemptsMap.containsKey(key)) {
      maxAttemptsMap.put(key, maxAttempts);
      windowSecondsMap.put(key, windowSeconds);
    }

    int currentAttempts = attempts.getOrDefault(key, 0) + 1;
    attempts.put(key, currentAttempts);

    int limit = maxAttemptsMap.getOrDefault(key, maxAttempts);
    return currentAttempts <= limit;
  }

  @Override
  public int getRetryAfterSeconds(String key) {
    int limit = maxAttemptsMap.getOrDefault(key, 5);
    int current = attempts.getOrDefault(key, 0);

    if (current < limit) {
      return 0;
    }

    return windowSecondsMap.getOrDefault(key, 900); // Default 15 minutes
  }
}
