/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import cm.jemil.proxi.gaz.application.outbound.Clock;
import cm.jemil.proxi.gaz.application.outbound.VendorSessionRepository;
import cm.jemil.proxi.gaz.domain.shared.LastUsedAt;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateVendorSessionUseCase {
  private final VendorSessionRepository vendorSessionRepository;
  private final Clock clock;
  // Simple in-memory throttling: vendorId -> last update timestamp (in ms)
  // This is per-instance; for clustered deployments, use a distributed cache
  private final Map<UUID, Long> lastUpdateMap = new ConcurrentHashMap<>();
  private static final long THROTTLE_MS = 60_000; // 1 minute

  public void execute(VendorId vendorId) {

    // Update last_used_at (throttled to once per minute)
    var now = clock.now();
    var lastUpdate = lastUpdateMap.getOrDefault(vendorId.value(), 0L);
    if (now.toEpochMilli() - lastUpdate >= THROTTLE_MS) {
      vendorSessionRepository.updateLastUsedAt(vendorId, LastUsedAt.from(now));
      lastUpdateMap.put(vendorId.value(), now.toEpochMilli());
    }
  }
}
