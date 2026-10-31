/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.views;

import cm.jemil.proxi.gaz.domain.shared.AddressText;
import cm.jemil.proxi.gaz.domain.shared.GeoPoint;
import cm.jemil.proxi.gaz.domain.shared.Money;
import cm.jemil.proxi.gaz.domain.shared.PhoneNumber;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import cm.jemil.proxi.gaz.domain.vendor.DeliveryFeeType;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import cm.jemil.proxi.gaz.domain.vendor.VendorName;

public interface VendorView {

  interface VendorView1 extends VendorView {
    VendorId getVendorId();

    UserId getUserId();

    VendorName getName();

    PhoneNumber getPhone();

    GeoPoint getLocation();

    AddressText getAddressText();

    boolean getIsStation();

    DeliveryFeeType getDeliveryFeeType();

    Money getDeliveryFeeAmount();

    boolean getIsActive();
  }
}
