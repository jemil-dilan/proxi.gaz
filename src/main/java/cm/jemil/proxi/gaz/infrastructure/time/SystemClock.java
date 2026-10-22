/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.time;

import cm.jemil.proxi.gaz.application.outbound.Clock;
import java.time.Instant;

/**
 * Real clock implementation that returns the actual current time. Used in production and non-test
 * environments.
 */
public class SystemClock implements Clock {

  @Override
  public Instant now() {
    return Instant.now();
  }
}
