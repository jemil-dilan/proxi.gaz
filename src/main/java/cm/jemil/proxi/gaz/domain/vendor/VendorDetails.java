/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.vendor;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import cm.jemil.proxi.gaz.domain.shared.AddressText;
import cm.jemil.proxi.gaz.domain.shared.GeoPoint;
import cm.jemil.proxi.gaz.domain.shared.Money;
import cm.jemil.proxi.gaz.domain.shared.PhoneNumber;
import java.util.Optional;

public record VendorDetails(
    VendorName name,
    PhoneNumber phone,
    GeoPoint location,
    Optional<AddressText> addressText,
    boolean station,
    Optional<DeliveryFeeType> deliveryFeeType,
    Optional<Money> deliveryFeeAmount,
    boolean active) {
  public VendorDetails {
    if (name == null
        || phone == null
        || location == null
        || addressText == null
        || deliveryFeeType == null
        || deliveryFeeAmount == null) {
      throw new InvalidDomainValueException("vendor details are required");
    }
  }

  public static VendorDetails from(
      String name,
      String phone,
      Double latitude,
      Double longitude,
      String addressText,
      Boolean isStation,
      String deliveryFeeType,
      Integer deliveryFeeAmount,
      Boolean isActive) {
    if (latitude == null || longitude == null || isStation == null || isActive == null) {
      throw new InvalidDomainValueException("location, station flag, and active flag are required");
    }
    return new VendorDetails(
        VendorName.from(name),
        PhoneNumber.from(phone),
        GeoPoint.of(latitude, longitude),
        AddressText.fromOptional(addressText),
        isStation,
        DeliveryFeeType.fromOptional(deliveryFeeType),
        Optional.ofNullable(deliveryFeeAmount).map(Money::fcfa),
        isActive);
  }
}
