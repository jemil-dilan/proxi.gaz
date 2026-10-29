/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.vendor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import org.junit.jupiter.api.Test;

class VendorNameTest {

  @Test
  void from_shouldTrimName() {
    assertThat(VendorName.from(" Demo Vendor ").value()).isEqualTo("Demo Vendor");
  }

  @Test
  void from_shouldRejectBlankName() {
    assertThatThrownBy(() -> VendorName.from(" ")).isInstanceOf(InvalidDomainValueException.class);
  }

  @Test
  void from_shouldRejectNameLongerThanStorageLimit() {
    assertThatThrownBy(() -> VendorName.from("v".repeat(256)))
        .isInstanceOf(InvalidDomainValueException.class);
  }
}
