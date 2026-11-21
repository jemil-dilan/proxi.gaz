/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.vendor;

import static org.assertj.core.api.Assertions.assertThat;

import cm.jemil.proxi.gaz.domain.shared.AddressText;
import cm.jemil.proxi.gaz.domain.shared.GeoPoint;
import cm.jemil.proxi.gaz.domain.shared.Money;
import cm.jemil.proxi.gaz.domain.shared.PhoneNumber;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import org.junit.jupiter.api.Test;

class VendorTest {

  @Test
  void of_shouldHaveItsOwnIdentityAndReferenceTheOwningUser() {
    var userId = UserId.generate();
    var vendor = vendor(userId, "Vendor", true);

    assertThat(vendor.getId()).isNotNull();
    assertThat(vendor.getId().value()).isNotEqualTo(userId.value());
    assertThat(vendor.getUserId()).isEqualTo(userId);
    assertThat(vendor.getName()).isEqualTo(VendorName.from("Vendor"));
  }

  @Test
  void reconstitute_shouldPreserveVendorAndUserIdentityWithUpdatedProfile() {
    var userId = UserId.generate();
    var original = vendor(userId, "Before", true);
    var updated =
        Vendor.reconstitute(
            original.getId(),
            original.getUserId(),
            VendorName.from("After"),
            PhoneNumber.from("+237699002002"),
            GeoPoint.of(4.06, 9.71),
            AddressText.from("Akwa"),
            false,
            DeliveryFeeType.from("flat"),
            Money.fcfa(1000),
            false);

    assertThat(updated.getId()).isEqualTo(original.getId());
    assertThat(updated.getUserId()).isEqualTo(userId);
    assertThat(updated.getName()).isEqualTo(VendorName.from("After"));
    assertThat(updated.isActive()).isFalse();
  }

  @Test
  void activateAndDeactivate_shouldSetTheRequestedStatusIdempotently() {
    var vendor = vendor(UserId.generate(), "Vendor", false);

    vendor.activate();
    vendor.activate();
    assertThat(vendor.isActive()).isTrue();

    vendor.deactivate();
    vendor.deactivate();
    assertThat(vendor.isActive()).isFalse();
  }

  private Vendor vendor(UserId userId, String name, boolean active) {
    return Vendor.of(
        userId,
        VendorName.from(name),
        PhoneNumber.from("+237699002001"),
        GeoPoint.of(4.05, 9.70),
        AddressText.from("Akwa"),
        false,
        DeliveryFeeType.from("flat"),
        Money.fcfa(1200),
        active);
  }

  @Test
  void profileChangesTo_shouldListChangedFieldNamesInProfileOrder() {
    var before = profile(VendorName.from("Before"), "+237699002001", GeoPoint.of(4.05, 9.70), 1200);
    var after =
        Vendor.reconstitute(
            before.getId(),
            before.getUserId(),
            VendorName.from("After"),
            PhoneNumber.from("+237699002001"),
            GeoPoint.of(4.06, 9.70),
            AddressText.from("Deido"),
            true,
            DeliveryFeeType.from("flat"),
            Money.fcfa(1500),
            false);

    assertThat(before.profileChangesTo(after))
        .containsExactly("name", "location", "addressText", "isStation", "deliveryFeeAmount");
  }

  @Test
  void profileChangesTo_shouldBeEmptyWhenTheProfileIsUnchangedAndIgnoreStatus() {
    var before = profile(VendorName.from("Same"), "+237699002001", GeoPoint.of(4.05, 9.70), 1200);
    var sameProfileInactive =
        Vendor.reconstitute(
            before.getId(),
            before.getUserId(),
            before.getName(),
            before.getPhone(),
            before.getLocation(),
            before.getAddressText(),
            before.isStation(),
            before.getDeliveryFeeType(),
            before.getDeliveryFeeAmount(),
            false);

    assertThat(before.profileChangesTo(sameProfileInactive)).isEmpty();
  }

  private static Vendor profile(VendorName name, String phone, GeoPoint location, int fee) {
    return Vendor.reconstitute(
        VendorId.generate(),
        UserId.generate(),
        name,
        PhoneNumber.from(phone),
        location,
        AddressText.from("Akwa"),
        false,
        DeliveryFeeType.from("flat"),
        Money.fcfa(fee),
        true);
  }
}
