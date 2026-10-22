/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.vendor;

import java.time.Instant;

/**
 * Brand availability flags. Currently available iff {@code is_available AND available_until > now
 * AND is_open_now}.
 */
public record Availability(boolean available, Instant availableUntil, boolean openNow) {

  public static Availability of(boolean available, Instant availableUntil, boolean openNow) {
    return new Availability(available, availableUntil, openNow);
  }

  public boolean isCurrentlyAvailable(Instant now) {
    return available && openNow && availableUntil != null && availableUntil.isAfter(now);
  }
}
