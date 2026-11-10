/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.views;

import java.time.Instant;
import java.util.UUID;
import org.locationtech.jts.geom.Point;

public interface VendorView {

  interface VendorView1 extends VendorView {
    UUID getVendorId();

    UUID getUserId();

    String getName();

    String getPhone();

    Point getLocation();

    String getAddressText();

    boolean getIsStation();

    String getDeliveryFeeType();

    Integer getDeliveryFeeAmount();

    boolean getIsActive();

    Instant getCreatedAt();

    Instant getUpdatedAt();

    String getEmail();

    boolean getHasActiveSession();

    Instant getSessionCreatedAt();

    Instant getSessionLastUsedAt();
  }
}
