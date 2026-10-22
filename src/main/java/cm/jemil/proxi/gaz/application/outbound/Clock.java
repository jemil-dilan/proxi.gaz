/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.outbound;

import java.time.Instant;

/**
 * Port interface for abstracting time in the application. Enables testability of time-dependent
 * behavior (offer expiry, availability, auto-validation). All timing-sensitive domain logic must
 * use this port rather than direct system time.
 */
public interface Clock {

  /**
   * Returns the current instant in time.
   *
   * @return the current instant, not null
   */
  Instant now();
}
