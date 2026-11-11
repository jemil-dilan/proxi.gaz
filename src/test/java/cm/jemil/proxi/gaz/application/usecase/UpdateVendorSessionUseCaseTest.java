/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cm.jemil.proxi.gaz.application.outbound.Clock;
import cm.jemil.proxi.gaz.application.outbound.VendorSessionRepository;
import cm.jemil.proxi.gaz.domain.shared.LastUsedAt;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateVendorSessionUseCaseTest {

  @Mock private VendorSessionRepository vendorSessionRepository;
  @Mock private Clock clock;

  private UpdateVendorSessionUseCase useCase;
  private VendorId vendorId;

  @BeforeEach
  void setUp() {
    useCase = new UpdateVendorSessionUseCase(vendorSessionRepository, clock);
    vendorId = VendorId.generate();
  }

  @Test
  void execute_shouldUpdateLastUsedAtOnFirstCall() {
    var now = Instant.parse("2026-11-10T10:00:00Z");
    when(clock.now()).thenReturn(now);

    useCase.execute(vendorId);

    verify(vendorSessionRepository).updateLastUsedAt(vendorId, LastUsedAt.from(now));
  }

  @Test
  void execute_shouldThrottleUpdatesWithinOneMinute() {
    var first = Instant.parse("2026-11-10T10:00:00Z");
    var withinThrottle = Instant.parse("2026-11-10T10:00:30Z");
    when(clock.now()).thenReturn(first, withinThrottle);

    useCase.execute(vendorId);
    useCase.execute(vendorId);

    verify(vendorSessionRepository, times(1)).updateLastUsedAt(eq(vendorId), any(LastUsedAt.class));
  }

  @Test
  void execute_shouldUpdateAgainAfterThrottleWindow() {
    var first = Instant.parse("2026-11-10T10:00:00Z");
    var afterThrottle = Instant.parse("2026-11-10T10:01:00Z");
    when(clock.now()).thenReturn(first, afterThrottle);

    useCase.execute(vendorId);
    useCase.execute(vendorId);

    verify(vendorSessionRepository, times(2)).updateLastUsedAt(eq(vendorId), any(LastUsedAt.class));
  }

  @Test
  void execute_shouldTrackThrottlePerVendor() {
    var otherVendorId = VendorId.generate();
    var now = Instant.parse("2026-11-10T10:00:00Z");
    when(clock.now()).thenReturn(now);

    useCase.execute(vendorId);
    useCase.execute(otherVendorId);

    verify(vendorSessionRepository).updateLastUsedAt(vendorId, LastUsedAt.from(now));
    verify(vendorSessionRepository).updateLastUsedAt(otherVendorId, LastUsedAt.from(now));
  }
}
