/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.vendor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import org.junit.jupiter.api.Test;

class DeliveryFeeTypeTest {

  @Test
  void fromOptional_shouldReturnEmptyForMissingOrBlankType() {
    assertThat(DeliveryFeeType.fromOptional(null)).isEmpty();
    assertThat(DeliveryFeeType.fromOptional("  ")).isEmpty();
  }

  @Test
  void from_shouldTrimAndRetainType() {
    assertThat(DeliveryFeeType.from(" flat ").value()).isEqualTo("flat");
  }

  @Test
  void from_shouldRejectTypeLongerThanStorageLimit() {
    assertThatThrownBy(() -> DeliveryFeeType.from("x".repeat(33)))
        .isInstanceOf(InvalidDomainValueException.class);
  }
}
