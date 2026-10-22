/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ConfirmationCodeTest {

  @Test
  void from_acceptsFourDigits() {
    assertThat(ConfirmationCode.from("0421").value()).isEqualTo("0421");
  }

  @ParameterizedTest
  @ValueSource(strings = {"421", "04210", "abcd", "12a4", "", " 421"})
  void from_rejectsInvalid(String raw) {
    assertThatThrownBy(() -> ConfirmationCode.from(raw))
        .isInstanceOf(InvalidDomainValueException.class);
  }

  @Test
  void matches_comparesValue() {
    ConfirmationCode code = ConfirmationCode.from("1234");
    assertThat(code.matches("1234")).isTrue();
    assertThat(code.matches("9999")).isFalse();
  }
}
