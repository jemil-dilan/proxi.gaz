/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cm.jemil.proxi.gaz.application.usecase.CreateDemoUseCase;
import cm.jemil.proxi.gaz.application.usecase.CreateVendorUseCase;
import cm.jemil.proxi.gaz.application.usecase.UpdateDemoUseCase.Command;
import cm.jemil.proxi.gaz.application.views.DemoView;
import cm.jemil.proxi.gaz.application.views.DemoView.DemoView1;
import cm.jemil.proxi.gaz.application.views.DemoView.DemoView2;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.CreateVendorDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.CreationResponseDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.DemoCreateDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.DemoDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.DemoUpdateDTO;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RestMapperTest {

  private final RestMapper restMapper = new RestMapperImpl();

  private static final UUID DEMO_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");

  @Test
  void toCommand_shouldMapCreateDtoToCommand() {
    // Given
    var dto = new DemoCreateDTO().name("Demo").description("Description");

    // When
    CreateDemoUseCase.Command command = restMapper.toCommand(dto);

    // Then
    assertThat(command.name()).isEqualTo("Demo");
    assertThat(command.description()).isEqualTo("Description");
  }

  @Test
  void fromDomain_shouldMapUuidToCreationResponse() {
    // When
    CreationResponseDTO response = restMapper.fromDomain(DEMO_ID);

    // Then
    assertThat(response.getNewId()).isEqualTo(DEMO_ID);
  }

  @Test
  void fromDomain_shouldMapView1WithoutDescription() {
    // Given
    DemoView1 view1 = mock(DemoView1.class);
    when(view1.getDemoId()).thenReturn(DEMO_ID);
    when(view1.getName()).thenReturn("Demo");
    when(view1.getReference()).thenReturn("DEMO-001");

    // When
    DemoDTO dto = restMapper.fromDomain(view1);

    // Then
    assertThat(dto.getId()).isEqualTo(DEMO_ID);
    assertThat(dto.getName()).isEqualTo("Demo");
    assertThat(dto.getReference()).isEqualTo("DEMO-001");
    assertThat(dto.getDescription()).isNull();
  }

  @Test
  void fromDomain_shouldMapView2WithDescription() {
    // Given
    DemoView2 view2 = mock(DemoView2.class);
    when(view2.getDemoId()).thenReturn(DEMO_ID);
    when(view2.getName()).thenReturn("Demo");
    when(view2.getReference()).thenReturn("DEMO-001");
    when(view2.getDescription()).thenReturn("Description");

    // When
    DemoDTO dto = restMapper.fromDomain(view2);

    // Then
    assertThat(dto.getId()).isEqualTo(DEMO_ID);
    assertThat(dto.getName()).isEqualTo("Demo");
    assertThat(dto.getReference()).isEqualTo("DEMO-001");
    assertThat(dto.getDescription()).isEqualTo("Description");
  }

  @Test
  void fromDomain_shouldDispatchOnDemoViewInterface() {
    // Given
    DemoView1 view1 = mock(DemoView1.class);
    when(view1.getDemoId()).thenReturn(DEMO_ID);
    when(view1.getName()).thenReturn("Demo");
    when(view1.getReference()).thenReturn("DEMO-001");

    // When
    DemoDTO dto = restMapper.fromDomain((DemoView) view1);

    // Then
    assertThat(dto.getId()).isEqualTo(DEMO_ID);
  }

  @Test
  void toCommand_shouldMapUpdateDtoToCommand() {
    // Given
    var dto = new DemoUpdateDTO().name("Demo").description("Description");

    // When
    Command command = restMapper.toCommand(DEMO_ID, dto);

    // Then
    assertThat(command.demoId()).isEqualTo(DEMO_ID);
    assertThat(command.name()).isEqualTo("Demo");
    assertThat(command.description()).isEqualTo("Description");
  }

  @Test
  void toCreateVendorCommand_shouldAllowMissingEmail() {
    var dto =
        new CreateVendorDTO()
            .name("Vendor")
            .phone("+237699002001")
            .latitude(4.05)
            .longitude(9.70)
            .address("Akwa")
            .isStation(false)
            .isActive(true);

    var command = restMapper.toCommand(dto);

    assertThat(command.email()).isNull();
  }

  @Test
  void toCreateVendorCommand_shouldNormalizeOptionalEmail() {
    var dto =
        new CreateVendorDTO()
            .email("  Vendor@Example.Test ")
            .name("Vendor")
            .phone("+237699002001")
            .latitude(4.05)
            .longitude(9.70)
            .address("Akwa")
            .isStation(false)
            .isActive(true);

    var command = restMapper.toCommand(dto);

    assertThat(command.email()).isEqualTo(Email.from("vendor@example.test"));
  }

  @Test
  void fromDomain_shouldMapVendorIdAndOneTimeSessionToken() {
    var vendorId = VendorId.generate();
    var result = new CreateVendorUseCase.Response(vendorId, "one-time-token");

    var dto = restMapper.fromDomain(result);

    assertThat(dto.getVendorId()).isEqualTo(vendorId.value());
    assertThat(dto.getSessionToken()).isEqualTo("one-time-token");
  }
}
