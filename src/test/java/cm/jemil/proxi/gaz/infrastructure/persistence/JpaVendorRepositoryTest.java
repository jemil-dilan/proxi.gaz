/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cm.jemil.proxi.gaz.domain.shared.AddressText;
import cm.jemil.proxi.gaz.domain.shared.GeoPoint;
import cm.jemil.proxi.gaz.domain.shared.PhoneNumber;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import cm.jemil.proxi.gaz.domain.vendor.Vendor;
import cm.jemil.proxi.gaz.domain.vendor.VendorName;
import cm.jemil.proxi.gaz.domain.vendor.VendorNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class JpaVendorRepositoryTest {
  private final VendorSpringRepository springRepository = mock(VendorSpringRepository.class);
  private final JpaMapper jpaMapper = mock(JpaMapper.class);
  private final JpaVendorRepository repository =
      new JpaVendorRepository(springRepository, jpaMapper);

  @Test
  void update_shouldFailWhenVendorDisappearsBeforePersistence() {
    var vendor =
        Vendor.of(
            UserId.generate(),
            VendorName.from("Vendor"),
            PhoneNumber.from("+237699000000"),
            GeoPoint.of(4.05, 9.70),
            AddressText.from("Akwa"),
            false,
            null,
            null,
            true);
    when(springRepository.findAggregateById(vendor.id())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> repository.update(vendor)).isInstanceOf(VendorNotFoundException.class);
  }
}
