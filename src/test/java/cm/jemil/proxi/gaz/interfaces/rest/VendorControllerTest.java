/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cm.jemil.proxi.gaz.application.usecase.CreateVendorUseCase;
import cm.jemil.proxi.gaz.application.usecase.SetVendorStatusUseCase;
import cm.jemil.proxi.gaz.application.usecase.UpdateVendorUseCase;
import cm.jemil.proxi.gaz.domain.shared.AddressText;
import cm.jemil.proxi.gaz.domain.shared.GeoPoint;
import cm.jemil.proxi.gaz.domain.shared.Money;
import cm.jemil.proxi.gaz.domain.shared.PhoneNumber;
import cm.jemil.proxi.gaz.domain.vendor.DeliveryFeeType;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import cm.jemil.proxi.gaz.domain.vendor.VendorName;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.CreateVendorDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.VendorOnboardingResponseDTO;
import org.junit.jupiter.api.Test;

class VendorControllerTest {

  private final RestMapper restMapper = mock(RestMapper.class);
  private final CreateVendorUseCase createVendorUseCase = mock(CreateVendorUseCase.class);
  private final UpdateVendorUseCase updateVendorUseCase = mock(UpdateVendorUseCase.class);
  private final SetVendorStatusUseCase setVendorStatusUseCase = mock(SetVendorStatusUseCase.class);
  private final VendorController controller =
      new VendorController(
          restMapper, createVendorUseCase, updateVendorUseCase, setVendorStatusUseCase);

  @Test
  void createVendor_shouldReturnOneTimeSessionTokenWithoutCaching() {
    var result = new CreateVendorUseCase.Response(VendorId.generate(), "one-time-token");
    var responseBody =
        new VendorOnboardingResponseDTO()
            .vendorId(result.vendorId().value())
            .sessionToken(result.sessionToken());
    when(restMapper.toCommand(any(CreateVendorDTO.class)))
        .thenReturn(
            new CreateVendorUseCase.Command(
                null,
                VendorName.from("Vendor"),
                PhoneNumber.from("+237699002001"),
                GeoPoint.of(4.05, 9.70),
                AddressText.from("Akwa"),
                false,
                DeliveryFeeType.from("flat"),
                Money.fcfa(1200),
                true));
    when(createVendorUseCase.execute(any())).thenReturn(result);
    when(restMapper.fromDomain(result)).thenReturn(responseBody);

    var response = controller.createVendor(new CreateVendorDTO());

    assertThat(response.getStatusCode().value()).isEqualTo(201);
    assertThat(response.getBody().getSessionToken()).isEqualTo("one-time-token");
    assertThat(response.getHeaders().getCacheControl()).contains("no-store");
  }
}
