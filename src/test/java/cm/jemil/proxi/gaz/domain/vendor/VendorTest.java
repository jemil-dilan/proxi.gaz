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
}
