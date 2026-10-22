/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import org.junit.jupiter.api.Test;

class TrackingTokenTest {

  @Test
  void generate_createsNonBlankToken() {
    TrackingToken token = TrackingToken.generate();
    assertThat(token.value()).isNotBlank();
    assertThat(token.value()).hasSizeGreaterThanOrEqualTo(16);
  }

  @Test
  void from_acceptsNonBlank() {
    assertThat(TrackingToken.from("track-abc-123").value()).isEqualTo("track-abc-123");
  }

  @Test
  void from_rejectsBlank() {
    assertThatThrownBy(() -> TrackingToken.from("  "))
        .isInstanceOf(InvalidDomainValueException.class);
  }
}
