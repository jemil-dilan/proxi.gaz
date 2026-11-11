/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.vendor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import org.junit.jupiter.api.Test;

class VendorListSortTest {

  @Test
  void parse_nullOrBlank_shouldReturnDefaultNameAsc() {
    assertThat(VendorListSort.parse(null)).isEqualTo(VendorListSort.DEFAULT);
    assertThat(VendorListSort.parse(" ")).isEqualTo(VendorListSort.DEFAULT);
  }

  @Test
  void parse_supportedValues_shouldMapToFieldAndDirection() {
    assertThat(VendorListSort.parse("name,asc"))
        .isEqualTo(new VendorListSort(VendorListSort.Field.NAME, VendorListSort.Direction.ASC));
    assertThat(VendorListSort.parse("createdAt,desc"))
        .isEqualTo(
            new VendorListSort(VendorListSort.Field.CREATED_AT, VendorListSort.Direction.DESC));
  }

  @Test
  void parse_unsupportedValue_shouldFail() {
    assertThatThrownBy(() -> VendorListSort.parse("phone,asc"))
        .isInstanceOf(InvalidDomainValueException.class);
  }
}
