/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import org.junit.jupiter.api.Test;

class PasswordHashTest {

  @Test
  void from_shouldPreserveEncodedValue() {
    var hash = "$2a$10$encoded-value";

    assertThat(PasswordHash.from(hash).value()).isEqualTo(hash);
  }

  @Test
  void from_shouldRejectBlankValue() {
    assertThatThrownBy(() -> PasswordHash.from(" "))
        .isInstanceOf(InvalidDomainValueException.class);
  }
}
