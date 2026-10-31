/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.vendor;

import cm.jemil.proxi.gaz.domain.shared.AddressText;
import cm.jemil.proxi.gaz.domain.shared.GeoPoint;
import cm.jemil.proxi.gaz.domain.shared.Money;
import cm.jemil.proxi.gaz.domain.shared.PhoneNumber;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/** Aggregate root for vendor profile and lifecycle data, owned by one User account. */
@Getter
public class Vendor {

  private final VendorId id;
  private final UserId userId;
  private VendorName name;
  private PhoneNumber phone;
  private GeoPoint location;
  private AddressText addressText;
  private boolean isStation;
  private @Nullable DeliveryFeeType deliveryFeeType;
  private @Nullable Money deliveryFeeAmount;
  private boolean active;

  public Vendor(
      VendorId id,
      UserId userId,
      VendorName name,
      PhoneNumber phone,
      GeoPoint location,
      AddressText addressText,
      boolean isStation,
      @Nullable DeliveryFeeType deliveryFeeType,
      @Nullable Money deliveryFeeAmount,
      boolean active) {
    this.id = id;
    this.userId = userId;
    this.name = name;
    this.phone = phone;
    this.location = location;
    this.addressText = addressText;
    this.isStation = isStation;
    this.deliveryFeeType = deliveryFeeType;
    this.deliveryFeeAmount = deliveryFeeAmount;
    this.active = active;
  }

  public static Vendor of(
      UserId userId,
      VendorName name,
      PhoneNumber phone,
      GeoPoint location,
      AddressText addressText,
      boolean station,
      @Nullable DeliveryFeeType deliveryFeeType,
      @Nullable Money deliveryFeeAmount,
      boolean active) {
    return new Vendor(
        VendorId.generate(),
        userId,
        name,
        phone,
        location,
        addressText,
        station,
        deliveryFeeType,
        deliveryFeeAmount,
        active);
  }

  public static Vendor reconstitute(
      VendorId id,
      UserId userId,
      VendorName name,
      PhoneNumber phone,
      GeoPoint location,
      AddressText addressText,
      boolean station,
      @Nullable DeliveryFeeType deliveryFeeType,
      @Nullable Money deliveryFeeAmount,
      boolean active) {
    return new Vendor(
        id,
        userId,
        name,
        phone,
        location,
        addressText,
        station,
        deliveryFeeType,
        deliveryFeeAmount,
        active);
  }

  public UUID id() {
    return id.value();
  }

  public void activate() {
    active = true;
  }

  public void deactivate() {
    active = false;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Vendor vendor = (Vendor) o;
    return Objects.equals(id, vendor.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
