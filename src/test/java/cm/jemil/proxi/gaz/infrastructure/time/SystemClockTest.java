/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.time;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class SystemClockTest {

  @Test
  void now_ReturnsCurrentInstant() {
    var clock = new SystemClock();
    Instant before = Instant.now();
    Instant result = clock.now();
    Instant after = Instant.now();

    assertThat(result).isAfterOrEqualTo(before);
    assertThat(result).isBeforeOrEqualTo(after);
  }

  @Test
  void now_NeverReturnsNull() {
    var clock = new SystemClock();
    assertThat(clock.now()).isNotNull();
  }
}
