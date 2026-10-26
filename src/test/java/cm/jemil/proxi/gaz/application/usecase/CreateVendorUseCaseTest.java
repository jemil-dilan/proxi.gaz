/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import cm.jemil.proxi.gaz.application.outbound.VendorRepository;
import cm.jemil.proxi.gaz.domain.vendor.Vendor;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CreateVendorUseCaseTest {
  private final VendorRepository repository = mock(VendorRepository.class);
  private final CreateVendorUseCase useCase = new CreateVendorUseCase(repository);

  @Test
  void execute_shouldCreateVendorAndInsertIt() {
    var result =
        useCase.execute(
            new CreateVendorUseCase.Command(
                " Vendor ", "+237699002001", 4.05, 9.70, null, false, null, null, true));

    var captor = ArgumentCaptor.forClass(Vendor.class);
    verify(repository).insert(captor.capture());
    assertThat(captor.getValue().id()).isEqualTo(result.vendorId());
    assertThat(captor.getValue().details().name().value()).isEqualTo("Vendor");
  }
}
