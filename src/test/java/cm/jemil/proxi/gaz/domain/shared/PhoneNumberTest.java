/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import org.junit.jupiter.api.Test;

class PhoneNumberTest {

  @Test
  void from_shouldTrimSurroundingWhitespace() {
    assertThat(PhoneNumber.from(" +237699002001 ").value()).isEqualTo("+237699002001");
  }

  @Test
  void from_shouldRejectBlankValue() {
    assertThatThrownBy(() -> PhoneNumber.from(" ")).isInstanceOf(InvalidDomainValueException.class);
  }

  @Test
  void from_shouldRejectValueLongerThanStorageLimit() {
    assertThatThrownBy(() -> PhoneNumber.from("1".repeat(33)))
        .isInstanceOf(InvalidDomainValueException.class);
  }
}
