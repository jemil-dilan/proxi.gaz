/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GeoPointTest {

  @Test
  void of_acceptsValidCoordinates() {
    GeoPoint point = GeoPoint.of(3.8480, 11.5021);
    assertThat(point.latitude()).isEqualTo(3.8480);
    assertThat(point.longitude()).isEqualTo(11.5021);
  }

  @ParameterizedTest
  @CsvSource({"-90.1, 0", "90.1, 0", "0, -180.1", "0, 180.1"})
  void of_rejectsOutOfRange(double lat, double lng) {
    assertThatThrownBy(() -> GeoPoint.of(lat, lng)).isInstanceOf(InvalidDomainValueException.class);
  }
}
