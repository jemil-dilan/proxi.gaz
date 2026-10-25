/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cm.jemil.proxi.gaz.application.outbound.VendorRepository;
import cm.jemil.proxi.gaz.domain.vendor.Vendor;
import cm.jemil.proxi.gaz.domain.vendor.VendorDetails;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import cm.jemil.proxi.gaz.domain.vendor.VendorNotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UpdateVendorUseCaseTest {
  private final VendorRepository repository = mock(VendorRepository.class);
  private final UpdateVendorUseCase useCase = new UpdateVendorUseCase(repository);
  private final UUID id = UUID.randomUUID();

  @Test
  void execute_shouldReplaceDetailsAndCallUpdate() {
    var vendor =
        Vendor.create(
            VendorDetails.from(
                "Before", "+237699002001", 4.05, 9.70, null, false, null, null, true));
    when(repository.findById(VendorId.from(id))).thenReturn(Optional.of(vendor));

    useCase.execute(
        new UpdateVendorUseCase.Command(
            id, "After", "+237699002002", 4.06, 9.71, "Akwa", true, "flat", 1500, false));

    verify(repository).update(vendor);
    org.assertj.core.api.Assertions.assertThat(vendor.details().name().value()).isEqualTo("After");
    org.assertj.core.api.Assertions.assertThat(vendor.details().active()).isFalse();
  }

  @Test
  void execute_shouldFailWhenVendorDoesNotExist() {
    when(repository.findById(VendorId.from(id))).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                useCase.execute(
                    new UpdateVendorUseCase.Command(
                        id, "After", "+237699002002", 4.06, 9.71, null, false, null, null, true)))
        .isInstanceOf(VendorNotFoundException.class);
    verify(repository, never()).update(any(Vendor.class));
  }
}
