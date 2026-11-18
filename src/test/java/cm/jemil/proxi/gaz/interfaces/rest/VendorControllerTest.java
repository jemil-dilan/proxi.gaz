/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cm.jemil.proxi.gaz.application.outbound.VendorRepository;
import cm.jemil.proxi.gaz.application.usecase.CreateVendorUseCase;
import cm.jemil.proxi.gaz.application.usecase.GetAllVendorsUseCase;
import cm.jemil.proxi.gaz.application.usecase.GetVendorByIdUseCase;
import cm.jemil.proxi.gaz.application.usecase.GetVendorSessionUseCase;
import cm.jemil.proxi.gaz.application.usecase.RevokeVendorSessionUseCase;
import cm.jemil.proxi.gaz.application.usecase.RotateVendorSessionUseCase;
import cm.jemil.proxi.gaz.application.usecase.SetVendorStatusUseCase;
import cm.jemil.proxi.gaz.application.usecase.UpdateVendorUseCase;
import cm.jemil.proxi.gaz.application.views.VendorView;
import cm.jemil.proxi.gaz.domain.shared.AddressText;
import cm.jemil.proxi.gaz.domain.shared.GeoPoint;
import cm.jemil.proxi.gaz.domain.shared.Money;
import cm.jemil.proxi.gaz.domain.shared.PhoneNumber;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import cm.jemil.proxi.gaz.domain.user.InvalidAdminTokenException;
import cm.jemil.proxi.gaz.domain.vendor.DeliveryFeeType;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import cm.jemil.proxi.gaz.domain.vendor.VendorName;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.CreateVendorDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.VendorDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.VendorOnboardingResponseDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.VendorPaginationDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.VendorStatusUpdateDTO;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@ExtendWith(MockitoExtension.class)
class VendorControllerTest {
  @Mock private RestMapper restMapper;
  @Mock private CreateVendorUseCase createVendorUseCase;
  @Mock private GetVendorByIdUseCase getVendorByIdUseCase;
  @Mock private UpdateVendorUseCase updateVendorUseCase;
  @Mock private SetVendorStatusUseCase setVendorStatusUseCase;
  @Mock private GetAllVendorsUseCase getAllVendorsUseCase;
  @Mock private RotateVendorSessionUseCase rotateVendorSessionUseCase;
  @Mock private RevokeVendorSessionUseCase revokeVendorSessionUseCase;
  @Mock private VendorRepository vendorRepository;
  @Mock private GetVendorSessionUseCase getVendorSessionUseCase;

  @InjectMocks private VendorController controller;

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void setVendorStatus_shouldPassTheAuthenticatedAdminToTheUseCase() {
    var adminId = UUID.randomUUID();
    var vendorId = UUID.randomUUID();
    authenticateAdmin(adminId.toString());

    var response =
        controller.setVendorStatus(vendorId, new VendorStatusUpdateDTO().isActive(false));

    assertThat(response.getStatusCode().value()).isEqualTo(204);
    verify(setVendorStatusUseCase).execute(UserId.from(adminId), vendorId, false);
  }

  @Test
  void adminWrites_shouldRejectATokenWhoseSubjectIsNotAUserId() {
    authenticateAdmin("not-a-uuid");
    var vendorId = UUID.randomUUID();

    assertThatThrownBy(() -> controller.revokeVendorSession(vendorId))
        .isInstanceOf(InvalidAdminTokenException.class);
    verifyNoInteractions(revokeVendorSessionUseCase);
  }

  @Test
  void adminWrites_shouldRejectARequestWithoutAnAdminToken() {
    var vendorId = UUID.randomUUID();

    assertThatThrownBy(() -> controller.rotateVendorSession(vendorId))
        .isInstanceOf(InvalidAdminTokenException.class);
    verifyNoInteractions(rotateVendorSessionUseCase);
  }

  private static void authenticateAdmin(String subject) {
    var jwt =
        Jwt.withTokenValue("token")
            .header("alg", "HS256")
            .subject(subject)
            .claim("scope", "admin")
            .build();
    SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
  }

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
    var adminId = UUID.randomUUID();
    authenticateAdmin(adminId.toString());
    when(createVendorUseCase.execute(eq(UserId.from(adminId)), any())).thenReturn(result);
    when(restMapper.fromDomain(result)).thenReturn(responseBody);

    var response = controller.createVendor(new CreateVendorDTO());

    assertThat(response.getStatusCode().value()).isEqualTo(201);
    assertThat(response.getBody().getSessionToken()).isEqualTo("one-time-token");
    assertThat(response.getHeaders().getCacheControl()).contains("no-store");
  }

  @Test
  void listVendors_shouldReturnPaginatedVendors() {
    var vendorView = mock(VendorView.VendorView1.class);
    var useCaseResponse = new GetAllVendorsUseCase.Response(List.of(vendorView), 15L, 2, 10, 1);
    var vendorDTO = new VendorDTO().vendorId(UUID.randomUUID()).name("Test Vendor");
    var paginationDTO =
        new VendorPaginationDTO()
            .content(List.of(vendorDTO))
            .page(1)
            .size(10)
            .totalElements(15L)
            .totalPages(2);

    when(getAllVendorsUseCase.execute(any(GetAllVendorsUseCase.Query.class)))
        .thenReturn(useCaseResponse);
    when(restMapper.fromDomain(useCaseResponse)).thenReturn(paginationDTO);

    var response = controller.listVendors(1, 10, false, "Akwa", "name,desc");

    assertThat(response.getStatusCode().value()).isEqualTo(200);
    assertThat(response.getBody().getPage()).isEqualTo(1);
    assertThat(response.getBody().getSize()).isEqualTo(10);
    assertThat(response.getBody().getTotalElements()).isEqualTo(15);
    assertThat(response.getBody().getTotalPages()).isEqualTo(2);
    assertThat(response.getBody().getContent()).hasSize(1);

    verify(getAllVendorsUseCase)
        .execute(
            argThat(
                q ->
                    q.page() == 1
                        && q.size() == 10
                        && q.isActive() == false
                        && q.name().equals("Akwa")
                        && q.sort().equals("name,desc")));
    verify(restMapper).fromDomain(useCaseResponse);
  }

  @Test
  void listVendors_withDefaults_shouldUseDefaultPageAndSize() {
    var useCaseResponse = new GetAllVendorsUseCase.Response(List.of(), 0L, 0, 20, 0);
    var paginationDTO =
        new VendorPaginationDTO()
            .content(List.of())
            .page(0)
            .size(20)
            .totalElements(0L)
            .totalPages(0);

    when(getAllVendorsUseCase.execute(any(GetAllVendorsUseCase.Query.class)))
        .thenReturn(useCaseResponse);
    when(restMapper.fromDomain(useCaseResponse)).thenReturn(paginationDTO);

    var response = controller.listVendors(null, null, null, null, null);

    assertThat(response.getStatusCode().value()).isEqualTo(200);
    assertThat(response.getBody().getPage()).isZero();
    assertThat(response.getBody().getSize()).isEqualTo(20);

    verify(getAllVendorsUseCase)
        .execute(
            argThat(
                q ->
                    q.page() == 0
                        && q.size() == 20
                        && q.isActive() == null
                        && q.name() == null
                        && q.sort() == null));
  }

  @Test
  void listVendors_withActiveTrue_shouldPassActiveFilter() {
    var useCaseResponse = new GetAllVendorsUseCase.Response(List.of(), 5L, 1, 20, 0);
    var paginationDTO =
        new VendorPaginationDTO()
            .content(List.of())
            .page(0)
            .size(20)
            .totalElements(5L)
            .totalPages(1);

    when(getAllVendorsUseCase.execute(any(GetAllVendorsUseCase.Query.class)))
        .thenReturn(useCaseResponse);
    when(restMapper.fromDomain(useCaseResponse)).thenReturn(paginationDTO);

    var response = controller.listVendors(0, 20, true, null, "createdAt,asc");

    assertThat(response.getStatusCode().value()).isEqualTo(200);
    assertThat(response.getBody().getTotalElements()).isEqualTo(5);

    verify(getAllVendorsUseCase)
        .execute(
            argThat(
                q -> q.isActive() == true && q.name() == null && q.sort().equals("createdAt,asc")));
  }
}
