/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class CreatedAtTest {

  @Test
  void from_shouldKeepInstant() {
    assertThat(CreatedAt.from(Instant.EPOCH).value()).isEqualTo(Instant.EPOCH);
  }

  @Test
  void from_shouldRejectNull() {
    assertThatThrownBy(() -> CreatedAt.from(null)).isInstanceOf(InvalidDomainValueException.class);
  }
}
