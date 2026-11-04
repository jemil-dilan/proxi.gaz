/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.user;

import cm.jemil.proxi.gaz.domain.DomainException;
import cm.jemil.proxi.gaz.domain.ErrorCodes;

/**
 * Exception thrown when too many login attempts are made.
 */
public class TooManyLoginAttemptsException extends DomainException {
  private final int retryAfterSeconds;

  public TooManyLoginAttemptsException(int retryAfterSeconds) {
    super(ErrorCodes.PROXIGAZ_429_000, "Too many login attempts. Retry after " + retryAfterSeconds + " seconds.");
    this.retryAfterSeconds = retryAfterSeconds;
  }

  public int getRetryAfterSeconds() {
    return retryAfterSeconds;
  }
}
