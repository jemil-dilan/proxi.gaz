/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import org.junit.jupiter.api.Test;

class MoneyTest {

  @Test
  void fcfa_acceptsZeroAndPositive() {
    assertThat(Money.fcfa(0).amountFcfa()).isZero();
    assertThat(Money.fcfa(1500).amountFcfa()).isEqualTo(1500);
  }

  @Test
  void fcfa_rejectsNegative() {
    assertThatThrownBy(() -> Money.fcfa(-1)).isInstanceOf(InvalidDomainValueException.class);
  }

  @Test
  void add_sumsAmounts() {
    assertThat(Money.fcfa(1000).add(Money.fcfa(500))).isEqualTo(Money.fcfa(1500));
  }
}
