/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.vendor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import cm.jemil.proxi.gaz.domain.shared.AddressText;
import cm.jemil.proxi.gaz.domain.shared.GeoPoint;
import cm.jemil.proxi.gaz.domain.shared.Money;
import org.junit.jupiter.api.Test;

class VendorDetailsTest {

  @Test
  void from_shouldBuildVendorDetailsAndAllowOptionalValuesToBeAbsent() {
    var details =
        VendorDetails.from(
            "Demo Vendor", "+237699002001", 4.05, 9.70, null, false, null, null, true);

    assertThat(details.location()).isEqualTo(GeoPoint.of(4.05, 9.70));
    assertThat(details.addressText()).isEmpty();
    assertThat(details.deliveryFeeType()).isEmpty();
    assertThat(details.deliveryFeeAmount()).isEmpty();
  }

  @Test
  void from_shouldMapOptionalValues() {
    var details =
        VendorDetails.from(
            "Demo Vendor", "+237699002001", 4.05, 9.70, "Akwa", false, "flat", 1250, true);

    assertThat(details.addressText()).contains(AddressText.from("Akwa"));
    assertThat(details.deliveryFeeType()).contains(DeliveryFeeType.from("flat"));
    assertThat(details.deliveryFeeAmount()).contains(Money.fcfa(1250));
  }

  @Test
  void from_shouldRejectMissingRequiredLocationOrFlags() {
    assertThatThrownBy(
            () ->
                VendorDetails.from(
                    "Demo Vendor", "+237699002001", null, 9.70, null, false, null, null, true))
        .isInstanceOf(InvalidDomainValueException.class);
    assertThatThrownBy(
            () ->
                VendorDetails.from(
                    "Demo Vendor", "+237699002001", 4.05, 9.70, null, null, null, null, true))
        .isInstanceOf(InvalidDomainValueException.class);
  }

  @Test
  void from_shouldRejectNegativeDeliveryFee() {
    assertThatThrownBy(
            () ->
                VendorDetails.from(
                    "Demo Vendor", "+237699002001", 4.05, 9.70, null, false, "flat", -1, true))
        .isInstanceOf(InvalidDomainValueException.class);
  }
}
