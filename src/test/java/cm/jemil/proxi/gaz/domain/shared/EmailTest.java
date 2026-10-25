/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import org.junit.jupiter.api.Test;

class EmailTest {

  @Test
  void from_shouldTrimAndNormalizeCase() {
    assertThat(Email.from("  ADMIN@Example.com ").value()).isEqualTo("admin@example.com");
  }

  @Test
  void from_shouldRejectInvalidEmail() {
    assertThatThrownBy(() -> Email.from("invalid-email"))
        .isInstanceOf(InvalidDomainValueException.class);
  }

  @Test
  void from_shouldRejectNullEmail() {
    assertThatThrownBy(() -> Email.from(null)).isInstanceOf(InvalidDomainValueException.class);
  }
}
