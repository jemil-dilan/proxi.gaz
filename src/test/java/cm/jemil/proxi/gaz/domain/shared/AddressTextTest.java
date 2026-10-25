/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import org.junit.jupiter.api.Test;

class AddressTextTest {

  @Test
  void fromOptional_shouldReturnEmptyForMissingOrBlankText() {
    assertThat(AddressText.fromOptional(null)).isEmpty();
    assertThat(AddressText.fromOptional("  ")).isEmpty();
  }

  @Test
  void fromOptional_shouldTrimAndRetainAddress() {
    assertThat(AddressText.fromOptional(" Akwa, Douala "))
        .contains(AddressText.from("Akwa, Douala"));
  }

  @Test
  void from_shouldRejectTextLongerThanStorageLimit() {
    assertThatThrownBy(() -> AddressText.from("x".repeat(513)))
        .isInstanceOf(InvalidDomainValueException.class);
  }
}
