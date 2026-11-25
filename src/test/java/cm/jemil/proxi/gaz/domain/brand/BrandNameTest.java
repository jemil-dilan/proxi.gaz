/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.brand;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class BrandNameTest {

  @Test
  void from_shouldTrimAndCollapseInnerWhitespace() {
    assertThat(BrandName.from("  Green \t  Gas  ").value()).isEqualTo("Green Gas");
  }

  @Test
  void from_shouldKeepTheDisplayCase() {
    assertThat(BrandName.from("TotalEnergies").value()).isEqualTo("TotalEnergies");
  }

  @Test
  void key_shouldIgnoreCaseAndSpacing() {
    assertThat(BrandName.from("  green   GAS ").key()).isEqualTo(BrandName.from("Green Gas").key());
    assertThat(BrandName.from("Green Gas").key()).isEqualTo("green gas");
  }

  @Test
  void from_shouldAcceptSixtyFourCharacters() {
    var name = "a".repeat(64);
    assertThat(BrandName.from(name).value()).hasSize(64);
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "   ", "\t\n"})
  void from_shouldRejectMissingNames(String value) {
    assertThatThrownBy(() -> BrandName.from(value)).isInstanceOf(InvalidDomainValueException.class);
  }

  @Test
  void from_shouldRejectNamesLongerThanSixtyFourCharactersAfterNormalising() {
    var name = "a".repeat(65);
    assertThatThrownBy(() -> BrandName.from(name)).isInstanceOf(InvalidDomainValueException.class);
  }

  @Test
  void from_shouldMeasureTheNormalisedName() {
    var padded = "  " + "a".repeat(64) + "  ";
    assertThat(BrandName.from(padded).value()).hasSize(64);
  }
}
