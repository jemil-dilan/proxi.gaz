/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import cm.jemil.proxi.gaz.domain.vendor.VendorName;
import cm.jemil.proxi.gaz.domain.vendor.VendorNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class UpdateVendorUseCaseTest {

  private final VendorRepository repository = mock(VendorRepository.class);
  private final UpdateVendorUseCase useCase = new UpdateVendorUseCase(repository);

  @Test
  void execute_shouldUpdateProfileAndPreserveVendorAndUserIdentity() {
    var userId = UserId.generate();
    var vendor = vendor(VendorId.generate(), userId, "Before", true);
    when(repository.findById(vendor.getId())).thenReturn(vendor);

    useCase.execute(
        new UpdateVendorUseCase.Command(
            vendor.getId(),
            VendorName.from("After"),
            PhoneNumber.from("+237699002002"),
            GeoPoint.of(4.06, 9.71),
            AddressText.from("Akwa"),
            false,
            DeliveryFeeType.from("flat"),
            Money.fcfa(1500),
            false));

    var updatedCaptor = ArgumentCaptor.forClass(Vendor.class);
    verify(repository).update(updatedCaptor.capture());
    var updated = updatedCaptor.getValue();
    assertThat(updated.getId()).isEqualTo(vendor.getId());
    assertThat(updated.getUserId()).isEqualTo(userId);
    assertThat(updated.getName()).isEqualTo(VendorName.from("After"));
    assertThat(updated.isActive()).isFalse();
  }

  @Test
  void execute_shouldFailWhenVendorDoesNotExist() {
    var vendorId = VendorId.generate();
    when(repository.findById(vendorId)).thenThrow(new VendorNotFoundException());

    assertThatThrownBy(
            () ->
                useCase.execute(
                    new UpdateVendorUseCase.Command(
                        vendorId,
                        VendorName.from("After"),
                        PhoneNumber.from("+237699002002"),
                        GeoPoint.of(4.06, 9.71),
                        AddressText.from("Akwa"),
                        false,
                        DeliveryFeeType.from("flat"),
                        Money.fcfa(1500),
                        true)))
        .isInstanceOf(VendorNotFoundException.class);
    verify(repository, never()).update(any(Vendor.class));
  }

  private Vendor vendor(VendorId id, UserId userId, String name, boolean active) {
    return Vendor.reconstitute(
        id,
        userId,
        VendorName.from(name),
        PhoneNumber.from("+237699002001"),
        GeoPoint.of(4.05, 9.70),
        AddressText.from("Akwa"),
        false,
        DeliveryFeeType.from("flat"),
        Money.fcfa(1200),
        active);
  }
}
