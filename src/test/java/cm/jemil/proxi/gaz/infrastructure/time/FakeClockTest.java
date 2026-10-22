/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.time;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class FakeClockTest {

  @Test
  void now_ReturnsFixedInstant() {
    Instant fixedInstant = Instant.parse("2026-01-01T00:00:00Z");
    var clock = new FakeClock(fixedInstant);

    assertThat(clock.now()).isEqualTo(fixedInstant);
  }

  @Test
  void now_ReturnsSameInstantOnMultipleCalls() {
    Instant fixedInstant = Instant.parse("2026-01-01T00:00:00Z");
    var clock = new FakeClock(fixedInstant);

    Instant first = clock.now();
    Instant second = clock.now();
    Instant third = clock.now();

    assertThat(first).isEqualTo(second);
    assertThat(second).isEqualTo(third);
    assertThat(first).isEqualTo(fixedInstant);
  }

  @Test
  void setFixedInstant_UpdatesReturnedValue() {
    Instant firstInstant = Instant.parse("2026-01-01T00:00:00Z");
    Instant secondInstant = Instant.parse("2026-06-01T00:00:00Z");
    var clock = new FakeClock(firstInstant);

    assertThat(clock.now()).isEqualTo(firstInstant);

    clock.setFixedInstant(secondInstant);

    assertThat(clock.now()).isEqualTo(secondInstant);
  }

  @Test
  void now_NeverReturnsNull() {
    var clock = new FakeClock(Instant.now());
    assertThat(clock.now()).isNotNull();
  }
}
