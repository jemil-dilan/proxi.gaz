/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.time;

import cm.jemil.proxi.gaz.application.outbound.Clock;
import java.time.Instant;

/**
 * Test clock implementation that returns a fixed instant. Allows tests to control time precisely.
 */
public class FakeClock implements Clock {

  private Instant fixedInstant;

  public FakeClock(Instant fixedInstant) {
    this.fixedInstant = fixedInstant;
  }

  @Override
  public Instant now() {
    return fixedInstant;
  }

  public void setFixedInstant(Instant fixedInstant) {
    this.fixedInstant = fixedInstant;
  }
}
