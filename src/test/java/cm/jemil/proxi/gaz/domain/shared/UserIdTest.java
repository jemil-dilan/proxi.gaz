/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserIdTest {

  @Test
  void generate_shouldCreateAnId() {
    assertThat(UserId.generate().value()).isNotNull();
  }

  @Test
  void from_shouldKeepValue() {
    var id = UUID.randomUUID();

    assertThat(UserId.from(id).value()).isEqualTo(id);
  }

  @Test
  void from_shouldRejectNull() {
    assertThatThrownBy(() -> UserId.from(null)).isInstanceOf(InvalidDomainValueException.class);
  }
}
