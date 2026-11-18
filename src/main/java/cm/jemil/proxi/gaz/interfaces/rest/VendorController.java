/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.interfaces.rest;

import cm.jemil.proxi.gaz.application.usecase.CreateVendorUseCase;
import cm.jemil.proxi.gaz.application.usecase.GetAllVendorsUseCase;
import cm.jemil.proxi.gaz.application.usecase.GetAllVendorsUseCase.Query;
import cm.jemil.proxi.gaz.application.usecase.GetVendorByIdUseCase;
import cm.jemil.proxi.gaz.application.usecase.GetVendorSessionUseCase;
import cm.jemil.proxi.gaz.application.usecase.RevokeVendorSessionUseCase;
import cm.jemil.proxi.gaz.application.usecase.RotateVendorSessionUseCase;
import cm.jemil.proxi.gaz.application.usecase.SetVendorStatusUseCase;
import cm.jemil.proxi.gaz.application.usecase.UpdateVendorUseCase;
import cm.jemil.proxi.gaz.config.VendorPrincipal;
import cm.jemil.proxi.gaz.domain.vendor.InvalidVendorSessionException;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.api.VendorApi;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.CreateVendorDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.VendorDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.VendorOnboardingResponseDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.VendorPaginationDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.VendorSessionIdentityDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.VendorSessionTokenDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.VendorStatusUpdateDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.VendorUpdateDTO;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
class VendorController implements VendorApi {

  private final RestMapper restMapper;
  private final CreateVendorUseCase createVendorUseCase;
  private final GetVendorByIdUseCase getVendorByIdUseCase;
  private final UpdateVendorUseCase updateVendorUseCase;
  private final SetVendorStatusUseCase setVendorStatusUseCase;
  private final GetAllVendorsUseCase getAllVendorsUseCase;
  private final RotateVendorSessionUseCase rotateVendorSessionUseCase;
  private final RevokeVendorSessionUseCase revokeVendorSessionUseCase;
  private final GetVendorSessionUseCase getVendorSessionUseCase;

  @Override
  public ResponseEntity<VendorOnboardingResponseDTO> createVendor(CreateVendorDTO request) {
    var result = createVendorUseCase.execute(restMapper.toCommand(request));
    return ResponseEntity.status(201)
        .cacheControl(CacheControl.noStore())
        .body(restMapper.fromDomain(result));
  }

  @Override
  public ResponseEntity<VendorPaginationDTO> listVendors(
      Integer page, Integer size, Boolean isActive, String name, String sort) {
    var result =
        getAllVendorsUseCase.execute(
            new Query(page == null ? 0 : page, size == null ? 20 : size, isActive, name, sort));
    return ResponseEntity.ok().body(restMapper.fromDomain(result));
  }

  @Override
  public ResponseEntity<VendorDTO> getVendor(UUID vendorId) {
    var result = getVendorByIdUseCase.execute(vendorId);
    return ResponseEntity.ok(restMapper.toDTO(result));
  }

  @Override
  public ResponseEntity<Void> updateVendor(UUID vendorId, VendorUpdateDTO request) {
    updateVendorUseCase.execute(restMapper.toCommand(vendorId, request));
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> setVendorStatus(UUID vendorId, VendorStatusUpdateDTO request) {
    setVendorStatusUseCase.execute(vendorId, request.getIsActive());
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<VendorSessionIdentityDTO> getVendorSession() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();

    var vendorId =
        authentication != null && authentication.getPrincipal() instanceof VendorPrincipal(UUID id)
            ? id
            : null;

    if (vendorId == null) {
      throw new InvalidVendorSessionException();
    }

    var response = getVendorSessionUseCase.execute(vendorId);

    return ResponseEntity.ok(restMapper.fromDomain(response));
  }

  @Override
  public ResponseEntity<Void> revokeVendorSession(UUID vendorId) {
    revokeVendorSessionUseCase.execute(vendorId);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<VendorSessionTokenDTO> rotateVendorSession(UUID vendorId) {
    var token = rotateVendorSessionUseCase.execute(vendorId);
    return ResponseEntity.status(201)
        .cacheControl(CacheControl.noStore())
        .body(new VendorSessionTokenDTO(token));
  }
}
