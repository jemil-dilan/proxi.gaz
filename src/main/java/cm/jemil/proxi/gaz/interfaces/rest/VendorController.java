/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.interfaces.rest;

import cm.jemil.proxi.gaz.application.usecase.CreateVendorUseCase;
import cm.jemil.proxi.gaz.application.usecase.ListVendorsUseCase;
import cm.jemil.proxi.gaz.application.usecase.RevokeVendorSessionUseCase;
import cm.jemil.proxi.gaz.application.usecase.RotateVendorSessionUseCase;
import cm.jemil.proxi.gaz.application.usecase.SetVendorStatusUseCase;
import cm.jemil.proxi.gaz.application.usecase.UpdateVendorUseCase;
import cm.jemil.proxi.gaz.config.VendorPrincipal;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.api.VendorApi;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.CreateVendorDTO;
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
  private final UpdateVendorUseCase updateVendorUseCase;
  private final SetVendorStatusUseCase setVendorStatusUseCase;
  private final ListVendorsUseCase listVendorsUseCase;
  private final RotateVendorSessionUseCase rotateVendorSessionUseCase;
  private final RevokeVendorSessionUseCase revokeVendorSessionUseCase;

  @Override
  public ResponseEntity<VendorOnboardingResponseDTO> createVendor(CreateVendorDTO request) {
    var result = createVendorUseCase.execute(restMapper.toCommand(request));
    return ResponseEntity.status(201)
        .cacheControl(CacheControl.noStore())
        .body(restMapper.fromDomain(result));
  }

  @Override
  public ResponseEntity<VendorPaginationDTO> listVendors(
      Integer page, Integer size, Boolean isActive, String name) {
    var result =
        listVendorsUseCase.execute(
            new ListVendorsUseCase.Query(
                page == null ? 0 : page, size == null ? 20 : size, isActive, name));
    return ResponseEntity.ok().body(restMapper.fromDomain(result));
  }

  @Override
  public ResponseEntity<Void> updateVendor(UUID vendorId, VendorUpdateDTO request) {
    updateVendorUseCase.execute(restMapper.toCommand(vendorId, request));
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> setVendorStatus(UUID vendorId, VendorStatusUpdateDTO request) {
    setVendorStatusUseCase.execute(VendorId.from(vendorId), request.getIsActive());
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<VendorSessionIdentityDTO> getVendorSession() {
    var principal =
        (VendorPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return ResponseEntity.ok(new VendorSessionIdentityDTO(principal.vendorId()));
  }

  @Override
  public ResponseEntity<Void> revokeVendorSession(UUID vendorId) {
    revokeVendorSessionUseCase.execute(vendorId);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<VendorSessionTokenDTO> rotateVendorSession(UUID vendorId) {
    var token = rotateVendorSessionUseCase.execute(vendorId);
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(new VendorSessionTokenDTO(token));
  }
}
