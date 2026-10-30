/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cm.jemil.proxi.gaz.application.outbound.VendorRepository;
import cm.jemil.proxi.gaz.domain.shared.AddressText;
import cm.jemil.proxi.gaz.domain.shared.GeoPoint;
import cm.jemil.proxi.gaz.domain.shared.Money;
import cm.jemil.proxi.gaz.domain.shared.PhoneNumber;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import cm.jemil.proxi.gaz.domain.vendor.DeliveryFeeType;
import cm.jemil.proxi.gaz.domain.vendor.Vendor;
import cm.jemil.proxi.gaz.domain.vendor.VendorName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SetVendorStatusUseCaseTest {
  private final VendorRepository repository = mock(VendorRepository.class);
  private final SetVendorStatusUseCase useCase = new SetVendorStatusUseCase(repository);

  @Test
  void execute_shouldChangeOnlyTheRequestedVendorStatus() {
    var vendor =
        Vendor.of(
            UserId.generate(),
            VendorName.from("Vendor"),
            PhoneNumber.from("+237699000001"),
            GeoPoint.of(4.05, 9.70),
            AddressText.from("Akwa"),
            false,
            DeliveryFeeType.from("flat"),
            Money.fcfa(1000),
            true);
    when(repository.findById(vendor.getId())).thenReturn(vendor);

    useCase.execute(vendor.getId(), false);

    var captor = ArgumentCaptor.forClass(Vendor.class);
    verify(repository).update(captor.capture());
    assertThat(captor.getValue().isActive()).isFalse();
    assertThat(captor.getValue().getName()).isEqualTo(VendorName.from("Vendor"));
    assertThat(captor.getValue().getPhone()).isEqualTo(PhoneNumber.from("+237699000001"));
  }
}
