/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.vendor;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AvailabilityTest {

  private static final Instant NOW = Instant.parse("2026-10-22T12:00:00Z");
  private static final Instant FUTURE = Instant.parse("2026-10-22T16:00:00Z");
  private static final Instant PAST = Instant.parse("2026-10-22T10:00:00Z");

  @Test
  void isCurrentlyAvailable_trueWhenAllFlagsAndFutureUntil() {
    Availability availability = Availability.of(true, FUTURE, true);
    assertThat(availability.isCurrentlyAvailable(NOW)).isTrue();
  }

  @ParameterizedTest
  @CsvSource({
    "false, 2026-10-22T16:00:00Z, true",
    "true, 2026-10-22T16:00:00Z, false",
    "true, 2026-10-22T10:00:00Z, true"
  })
  void isCurrentlyAvailable_falseWhenAnyConditionFails(
      boolean available, Instant until, boolean openNow) {
    assertThat(Availability.of(available, until, openNow).isCurrentlyAvailable(NOW)).isFalse();
  }

  @Test
  void isCurrentlyAvailable_falseWhenAvailableUntilNull() {
    assertThat(Availability.of(true, null, true).isCurrentlyAvailable(NOW)).isFalse();
  }

  @Test
  void isCurrentlyAvailable_falseWhenUntilEqualsNow() {
    assertThat(Availability.of(true, NOW, true).isCurrentlyAvailable(NOW)).isFalse();
  }

  @Test
  void isCurrentlyAvailable_usesStrictlyAfterNow() {
    assertThat(Availability.of(true, PAST, true).isCurrentlyAvailable(NOW)).isFalse();
  }
}
